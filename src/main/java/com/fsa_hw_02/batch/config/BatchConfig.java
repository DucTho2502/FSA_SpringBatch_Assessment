package com.fsa_hw_02.batch.config;

import com.fsa_hw_02.batch.listener.JobCompletionListener;
import com.fsa_hw_02.batch.listener.PostProcessListener;
import com.fsa_hw_02.batch.model.PostDTO;
import com.fsa_hw_02.batch.writer.CustomItemWriter;
import com.fsa_hw_02.batch.writer.ErrorReportWriter;
import com.fsa_hw_02.exception.PostProcessingException;
import com.fsa_hw_02.model.Post;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableBatchProcessing(dataSourceRef = "batchDataSource", transactionManagerRef = "batchTransactionManager")
public class BatchConfig {

    @Bean
    public JobRepository jobRepository(
            @Qualifier("batchDataSource") DataSource batchDataSource,
            @Qualifier("batchTransactionManager") PlatformTransactionManager transactionManager
    ) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(batchDataSource);
        factory.setTransactionManager(transactionManager);
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean("importPostJob")
    public Job importPostJob(
            Step step1,
            JobCompletionListener listener,
            JobRepository jobRepository
    ) {
        return new JobBuilder("importPostJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(
            ItemReader<PostDTO> reader,
            ItemProcessor<PostDTO, Post> processor,
            CompositeItemWriter<Post> writer,
            JobRepository jobRepository,
            @Qualifier("batchTransactionManager") PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("step1", jobRepository)
                .<PostDTO, Post>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(PostProcessingException.class)
                .listener(postProcessListener())
                .build();
    }

    @Bean
    public FlatFileItemReader<PostDTO> reader() {
        FlatFileItemReader<PostDTO> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("posts.csv"));
        reader.setLinesToSkip(1); // <-- This skips the header line

        DefaultLineMapper<PostDTO> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "title", "content", "author");

        BeanWrapperFieldSetMapper<PostDTO> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(PostDTO.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public CompositeItemWriter<Post> compositeWriter(
            CustomItemWriter customItemWriter,
            ErrorReportWriter errorReportWriter
    ) {
        CompositeItemWriter<Post> writer = new CompositeItemWriter<>();
        writer.setDelegates(List.of(customItemWriter, errorReportWriter));
        return writer;
    }

    @Bean
    public PostProcessListener postProcessListener() {
        return new PostProcessListener();
    }

    @Bean
    public DataSourceInitializer batchSchemaInitializer(@Qualifier("batchDataSource") DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}

