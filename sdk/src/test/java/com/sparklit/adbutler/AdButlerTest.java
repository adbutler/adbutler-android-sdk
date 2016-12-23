package com.sparklit.adbutler;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class AdButlerTest {
    @Test
    public void testRequestPlacement() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("{\"status\": \"SUCCESS\", " +
                "\"placements\": {\"placement_1\": {" +
                "\"banner_id\": \"1\", " +
                "\"redirect_url\": \"https://servedbyadbutler.com/redirect.spark?MID=153105&plid=543820&setID=214764&channelID=0&CID=0&banID=519401954&PID=0&textadID=0&tc=1&mt=1482382937922945&hc=dc0133e390300a8f3db43edb21adbb3fd596b6d1&location=\", " +
                "\"image_url\": \"https://servedbyadbutler.com/default_banner.gif\", " +
                "\"width\": \"300\", " +
                "\"height\": \"250\", " +
                "\"alt_text\": \"\", " +
                "\"accompanied_html\": \"\", " +
                "\"target\": \"_blank\", " +
                "\"tracking_pixel\": \"\", " +
                "\"accupixel_url\": \"https://servedbyadbutler.com/adserve.ibs/;ID=153105;size=1x1;type=pixel;setID=214764;plid=543820;BID=519401954;wt=1482382947;rnd=68251\", " +
                "\"refresh_url\": \"\", " +
                "\"refresh_time\": \"\", " +
                "\"body\": \"\"}}}"));
        server.start();

        final CompletableFuture<PlacementResponse> success = new CompletableFuture<>();

        AdButler.ADBUTLER_ENDPOINT = "http://" + server.getHostName() + ":" + server.getPort() + "/";
        AdButler adbutler = new AdButler();
        PlacementRequestConfig config = new PlacementRequestConfig.Builder(153105, 214764, 300, 250).build();
        adbutler.requestPlacement(config, new PlacementResponseListener() {
            @Override
            public void success(PlacementResponse response) {
                success.complete(response);
            }

            @Override
            public void error(Throwable throwable) {
                throwable.printStackTrace();
                fail();
            }
        });

        assertEquals(server.takeRequest().getPath(), "/;ID=153105;size=300x250;setID=214764;type=json");
        PlacementResponse response = success.get(5, TimeUnit.SECONDS);
        assertEquals(response.getStatus(), "SUCCESS");
        assertEquals(response.getPlacements().size(), 1);
        Placement placement = response.getPlacements().get(0);
        assertEquals(placement.getBannerId(), 1);
        assertEquals(placement.getRedirectUrl(), "https://servedbyadbutler.com/redirect.spark?MID=153105&plid=543820&setID=214764&channelID=0&CID=0&banID=519401954&PID=0&textadID=0&tc=1&mt=1482382937922945&hc=dc0133e390300a8f3db43edb21adbb3fd596b6d1&location=");
        assertEquals(placement.getImageUrl(), "https://servedbyadbutler.com/default_banner.gif");
        assertEquals(placement.getWidth(), 300);
        assertEquals(placement.getHeight(), 250);
        assertEquals(placement.getAltText(), "");
        assertEquals(placement.getTarget(), "_blank");
        assertEquals(placement.getTrackingPixel(), "");
        assertEquals(placement.getAccupixelUrl(), "https://servedbyadbutler.com/adserve.ibs/;ID=153105;size=1x1;type=pixel;setID=214764;plid=543820;BID=519401954;wt=1482382947;rnd=68251");
        assertEquals(placement.getRefreshUrl(), "");
        assertEquals(placement.getRefreshTime(), "");
        assertEquals(placement.getBody(), "");

        server.shutdown();
    }

    @Test
    public void testRequestPlacements() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("{\"status\": \"SUCCESS\", " +
                "\"placements\": {\"placement_1\": {" +
                "\"banner_id\": \"1\", " +
                "\"redirect_url\": \"https://servedbyadbutler.com/redirect.spark?MID=153105&plid=543820&setID=214764&channelID=0&CID=0&banID=519401954&PID=0&textadID=0&tc=1&mt=1482382937922945&hc=dc0133e390300a8f3db43edb21adbb3fd596b6d1&location=\", " +
                "\"image_url\": \"https://servedbyadbutler.com/default_banner.gif\", " +
                "\"width\": \"300\", " +
                "\"height\": \"250\", " +
                "\"alt_text\": \"\", " +
                "\"accompanied_html\": \"\", " +
                "\"target\": \"_blank\", " +
                "\"tracking_pixel\": \"\", " +
                "\"accupixel_url\": \"https://servedbyadbutler.com/adserve.ibs/;ID=153105;size=1x1;type=pixel;setID=214764;plid=543820;BID=519401954;wt=1482382947;rnd=68251\", " +
                "\"refresh_url\": \"\", " +
                "\"refresh_time\": \"\", " +
                "\"body\": \"\"}}}"));
        server.enqueue(new MockResponse().setBody("{\"status\": \"SUCCESS\", " +
                "\"placements\": {\"placement_1\": {" +
                "\"banner_id\": \"2\", " +
                "\"redirect_url\": \"https://servedbyadbutler.com/redirect.spark?MID=153105&plid=550986&setID=214764&channelID=0&CID=0&banID=519407754&PID=0&textadID=0&tc=1&mt=1482382941414969&hc=0aafa9e4186a53ebb33094aea2f80c3a748cc7f0&location=\", " +
                "\"image_url\": \"https://servedbyadbutler.com/default_banner.gif\", " +
                "\"width\": \"300\", " +
                "\"height\": \"250\", " +
                "\"alt_text\": \"\", " +
                "\"accompanied_html\": \"\", " +
                "\"target\": \"_blank\", " +
                "\"tracking_pixel\": \"https://servedbyadbutler.com/default_banner.gif?foo=bar&demo=fakepixel\", " +
                "\"accupixel_url\": \"https://servedbyadbutler.com/adserve.ibs/;ID=153105;size=1x1;type=pixel;setID=214764;plid=543820;BID=519401954;wt=1482382947;rnd=68251\", " +
                "\"refresh_url\": \"\", " +
                "\"refresh_time\": \"\", " +
                "\"body\": \"\"}}}"));
        server.start();

        final CompletableFuture<PlacementResponse> success = new CompletableFuture<>();

        PlacementRequestConfig config1 = new PlacementRequestConfig.Builder(153105, 214764, 300, 250).build();
        String[] keywords = {"sample2", "test"};
        Set<String> keywordSet = new HashSet<>();
        Collections.addAll(keywordSet, keywords);
        PlacementRequestConfig config2 = new PlacementRequestConfig.Builder(153105, 214764, 300, 250)
                .setKeywords(keywordSet)
                .build();

        List<PlacementRequestConfig> configs = new ArrayList<>();
        configs.add(config1);
        configs.add(config2);

        AdButler.ADBUTLER_ENDPOINT = "http://" + server.getHostName() + ":" + server.getPort() + "/";
        AdButler adbutler = new AdButler();
        adbutler.requestPlacements(configs, new PlacementResponseListener() {
            @Override
            public void success(PlacementResponse response) {
                success.complete(response);
            }

            @Override
            public void error(Throwable throwable) {
                throwable.printStackTrace();
                fail();
            }
        });

        PlacementResponse response = success.get(5, TimeUnit.SECONDS);
        assertEquals(response.getStatus(), "SUCCESS");
        assertEquals(response.getPlacements().size(), 2);

        for (Placement placement : response.getPlacements()) {
            if (placement.getBannerId() == 1) {
                assertEquals(placement.getBannerId(), 1);
                assertEquals(placement.getRedirectUrl(), "https://servedbyadbutler.com/redirect.spark?MID=153105&plid=543820&setID=214764&channelID=0&CID=0&banID=519401954&PID=0&textadID=0&tc=1&mt=1482382937922945&hc=dc0133e390300a8f3db43edb21adbb3fd596b6d1&location=");
                assertEquals(placement.getImageUrl(), "https://servedbyadbutler.com/default_banner.gif");
                assertEquals(placement.getWidth(), 300);
                assertEquals(placement.getHeight(), 250);
                assertEquals(placement.getAltText(), "");
                assertEquals(placement.getTarget(), "_blank");
                assertEquals(placement.getTrackingPixel(), "");
                assertEquals(placement.getAccupixelUrl(), "https://servedbyadbutler.com/adserve.ibs/;ID=153105;size=1x1;type=pixel;setID=214764;plid=543820;BID=519401954;wt=1482382947;rnd=68251");
                assertEquals(placement.getRefreshUrl(), "");
                assertEquals(placement.getRefreshTime(), "");
                assertEquals(placement.getBody(), "");
            } else {
                assertEquals(placement.getBannerId(), 2);
                assertEquals(placement.getRedirectUrl(), "https://servedbyadbutler.com/redirect.spark?MID=153105&plid=550986&setID=214764&channelID=0&CID=0&banID=519407754&PID=0&textadID=0&tc=1&mt=1482382941414969&hc=0aafa9e4186a53ebb33094aea2f80c3a748cc7f0&location=");
                assertEquals(placement.getImageUrl(), "https://servedbyadbutler.com/default_banner.gif");
                assertEquals(placement.getWidth(), 300);
                assertEquals(placement.getHeight(), 250);
                assertEquals(placement.getAltText(), "");
                assertEquals(placement.getTarget(), "_blank");
                assertEquals(placement.getTrackingPixel(), "https://servedbyadbutler.com/default_banner.gif?foo=bar&demo=fakepixel");
                assertEquals(placement.getAccupixelUrl(), "https://servedbyadbutler.com/adserve.ibs/;ID=153105;size=1x1;type=pixel;setID=214764;plid=543820;BID=519401954;wt=1482382947;rnd=68251");
                assertEquals(placement.getRefreshUrl(), "");
                assertEquals(placement.getRefreshTime(), "");
                assertEquals(placement.getBody(), "");
            }
        }

        server.shutdown();
    }

    @Test
    public void testRequestPixel() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("{}"));
        server.start();

        String testBaseUrl = "http://" + server.getHostName() + ":" + server.getPort() + "/";
        String testUrl = testBaseUrl + "test";
        AdButler.ADBUTLER_ENDPOINT = "http://" + server.getHostName() + ":" + server.getPort() + "/";
        AdButler adbutler = new AdButler();
        adbutler.requestPixel(testUrl);

        assertEquals(server.takeRequest().getPath(), "/test");

        server.shutdown();
    }
}
