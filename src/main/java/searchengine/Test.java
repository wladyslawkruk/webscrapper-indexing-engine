package searchengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.thymeleaf.expression.Sets;
import searchengine.lemma.LemmaFinder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class Test {
    public static void main(String[] args) throws IOException {
//        String url = "https://mnmedical.ru/zamena-mlk-mlci2-na-agility-v-gbuz-moskovskaja-gorodskaja-onkologicheskaja-bolnica-62-dzm/";
//        Document document = Jsoup.connect(url).maxBodySize(0).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
//                .referrer("http://www.google.com").get();
//
//        String lemmas = document.getElementsByTag("body").text().replaceAll("<[^>]*>", "");
//        Map<String, Integer> lemmaMap = LemmaFinder.getInstance().collectLemmas(lemmas.toString());
//                for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
//            System.out.println(entry.getKey() + ":" + entry.getValue());
//        }
//        System.out.println(lemmaMap.size());
//        System.out.println(lemmas);

        List<String> newSet = Stream.of("оснащение","установка","онкология","лучевой","терапия").toList();
        StringBuilder query = new StringBuilder();
        for(String s:newSet){
            query.append(appendQuote(s));
        }
        query.deleteCharAt(query.length()-1);
        System.out.println(query);





        //System.out.println("------");
        //System.out.println(document.toString().replaceAll("<[^>]*>", ""));
//        String result = document.toString().replaceAll("<[^>]*>", "");
//        Map<String, Integer> lemmas1 = LemmaFinder.getInstance().collectLemmas(result[0]);
//        Map<String, Integer> lemmas2 = LemmaFinder.getInstance().collectLemmas(result[1]);
//        for (Map.Entry<String, Integer> entry : lemmas1.entrySet()) {
//            System.out.println(entry.getKey() + ":" + entry.getValue());
//        }
//        System.out.println("---------");
//        for (Map.Entry<String, Integer> entry : lemmas2.entrySet()) {
//            System.out.println(entry.getKey() + ":" + entry.getValue());
//        }

    }
    public static String appendQuote(String s) {
        return new StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .append(',')
                .toString();
    }
}
