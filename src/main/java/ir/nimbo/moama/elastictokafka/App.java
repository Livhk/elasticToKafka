package ir.nimbo.moama.elastictokafka;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
        ElasticManager elasticManager = new ElasticManager();
        try {
            elasticManager.scrollTest();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(-1);
    }
}
