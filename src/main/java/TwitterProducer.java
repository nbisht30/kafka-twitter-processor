import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {
    Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());
    String consumerKey = "QGIAvoxm7CvYAh6XI5zQEbzA4";
    String consumerSecret = "iOWDp8eSVceYic8b0uBk2DOoZLwmqMRgR5ZByxYBnqmbKdBYQg";
    String token = "960818204-1c8Sd4rB6JRDlw1mabaeEVt4GRh3qijRrKKFGWXe";
    String secret = "i3XKaPwNqGZgG4lZ1K6j6HnhfYJkecJ4L4vbH8NaZb5cd";

    public TwitterProducer() {

    }

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        //create a Twitter client
        Client client = createTwitterClient(msgQueue);
        client.connect();
        //create a Kafka producer

        //loop to send tweets to Kafka
        // on a different thread, or multiple different threads....
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(msg!= null){
                logger.info(msg);
            }
        }
        logger.info("End of application.");
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue) {
        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Search terms for twitter:-
        List<String> terms = Lists.newArrayList("bitcoin", "api");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();
        // Attempts to establish a connection.
        return hosebirdClient;
    }
}
