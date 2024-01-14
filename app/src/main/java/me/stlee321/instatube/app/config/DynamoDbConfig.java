package me.stlee321.instatube.app.config;

import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver;
import io.awspring.cloud.dynamodb.DynamoDbTableSchemaResolver;
import me.stlee321.instatube.app.service.dto.NotificationItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

@Configuration
public class DynamoDbConfig {

    @Value("${notification.dynamodb.table-name}")
    private String tableName;
    @Bean
    DynamoDbTableNameResolver dynamoDbTableNameResolver() {
        return new DynamoDbTableNameResolver() {
            @Override
            public <T> String resolve(Class<T> clazz) {
                return tableName;
            }
        };
    }
    StaticTableSchema notificationItemTableSchema() {
        return StaticTableSchema.builder(NotificationItem.class)
                .newItemSupplier(NotificationItem::new)
                .addAttribute(String.class, a -> a.name("target")
                        .getter(NotificationItem::getTarget)
                        .setter(NotificationItem::setTarget)
                        .tags(StaticAttributeTags.primaryPartitionKey()))
                .addAttribute(Long.class, a -> a.name("timestamp")
                        .getter(NotificationItem::getTimestamp)
                        .setter(NotificationItem::setTimestamp)
                        .tags(StaticAttributeTags.primarySortKey()))
                .addAttribute(String.class, a -> a.name("from")
                        .getter(NotificationItem::getFrom)
                        .setter(NotificationItem::setFrom))
                .addAttribute(String.class, a -> a.name("type")
                        .getter(NotificationItem::getType)
                        .setter(NotificationItem::setType))
                .addAttribute(String.class, a -> a.name("link")
                        .getter(NotificationItem::getLink)
                        .setter(NotificationItem::setLink))
                .build();
    }
    @Bean
    DynamoDbTableSchemaResolver dynamoDbTableSchemaResolver() {
        return new DynamoDbTableSchemaResolver() {
            @Override
            public <T> TableSchema resolve(Class<T> clazz) {
                return notificationItemTableSchema();
            }
        };
    }
}
