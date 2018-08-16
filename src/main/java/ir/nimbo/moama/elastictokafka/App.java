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
        elasticManager.scroll();
        System.exit(-1);
    }
}
