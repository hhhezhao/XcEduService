package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    // 查询全部记录
    @Test
    public void testSearchAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // source源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","description"},new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits searchHits = search.getHits();
        SearchHit[] hits = searchHits.getHits();
        for(SearchHit hit: hits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String)sourceAsMap.get("name");
            String studymodel = (String)sourceAsMap.get("studymodel");
            String description = (String)sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);

        }
    }

    // 分页查询
    @Test
    public void testSearchPage() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置分页参数
        // 页码
        int page = 1;
        // 每页记录数
        int size = 1;
        // 计算出记录起始下标
        int from = (page-1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        // 搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // source源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","description"},new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits searchHits = search.getHits();
        SearchHit[] hits = searchHits.getHits();
        for(SearchHit hit: hits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String)sourceAsMap.get("name");
            String studymodel = (String)sourceAsMap.get("studymodel");
            String description = (String)sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);

        }
    }

}
