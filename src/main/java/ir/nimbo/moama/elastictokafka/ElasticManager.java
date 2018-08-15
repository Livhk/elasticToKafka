package ir.nimbo.moama.elastictokafka;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.omg.PortableInterceptor.RequestInfoOperations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElasticManager {
    private RestHighLevelClient client;
    private String index = "pages";
    private SearchRequest searchRequest;
    private SearchSourceBuilder searchSourceBuilder;
    private KafkaManager kafkaManager;

    public ElasticManager() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("94.23.214.93", 9200, "http")));
        searchRequest = new SearchRequest(index);
        searchRequest.types("_doc");
        searchSourceBuilder = new SearchSourceBuilder();
        kafkaManager = new KafkaManager("retrievedTest", "master-node:9092,worker-node:9092");
    }


    public void scrollTest() throws IOException {
        Date begin = new Date();
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest("pages");
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(100);
        searchSourceBuilder.timeout(new TimeValue(90, TimeUnit.SECONDS));
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        boolean searchStatus = false;
        SearchResponse searchResponse = null;
        while (!searchStatus) {
            try {
                searchResponse = client.search(searchRequest);
                searchStatus = true;
            } catch (IOException e) {
                System.out.println("Elastic connection timed out! Trying again...");
                searchStatus = false;
            }
        }
        System.out.println("Partitioning Done!");
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        int i = 1, j = 0;
        while (j < 1000000) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchStatus = false;
            searchResponse = null;
            while (!searchStatus) {
                try {
                    searchResponse = client.search(searchRequest);
                    searchStatus = true;
                } catch (IOException e) {
                    System.out.println("Elastic connection timed out! Trying again...");
                    searchStatus = false;
                }
            }
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
            String[] links = new String[100];
            int k = 0;
            for(SearchHit hit : searchHits){
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//                System.out.println(i + "\t" + sourceAsMap.get("pageLink") + "\t" + j);
                links[k] = (String) sourceAsMap.get("pageLink");
                j++;
                k++;
            }
            kafkaManager.pushNewURL(links);
            System.out.println(i);
            i++;
        }
        Date end = new Date();
        System.out.println((end.getTime() - begin.getTime()) / 1000);
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest);
    }
}
