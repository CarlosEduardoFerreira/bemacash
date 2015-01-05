package com.kaching123.tcr.commands.print;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.print.printer.BasePosTextPrinter;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by vkompaniets on 06.11.2014.
 */
public class ParseHtmlCommand extends PublicGroundyTask {

    public static final String BOLD_PATTERN = "&&bold&&";
    private static final String ARG_URL = "ARG_URL";
    private static final String ARG_BEGIN_MARKER = "ARG_BEGIN_MARKER";
    private static final String ARG_END_MARKER = "ARG_END_MARKER";
    private static final String EXTRA_LINES = "EXTRA_LINES";

    private String url;
    private String beginMarker;
    private String endMarker;

    public static String br2nl(String html) {
        if(html==null)
            return html;

        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        document.select("td").prepend("   ");
        document.select("div").prepend("\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    public ArrayList<String> prepareReceiptLineFeed(String html) {
        html = html.replaceAll("(?i)<br[^>]*>", "br2n");
        html = html.replaceAll("(?i)<br/[^>]*>", "br2n");
        html = html.replaceAll("(?i)</tr[^>]*>", "br2n");
        html = html.replaceAll("(?i)</h[1-5]*>", BOLD_PATTERN+"br2n");
        html = html.replaceAll("(?i)</b>", BOLD_PATTERN);
        html = html.replaceAll("(?i)</td[^>]*>", "&nbsp;&nbsp;&nbsp;");
        html = html.replaceAll("(?i)</div[^>]*>", "br2n");

        String plainText = Jsoup.parse(html).text();
        plainText = plainText.replaceAll("br2n", "\n");
        String[] words = plainText.split("\\n+");
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("\n");
        for ( String tmp : words) {
            lines.add(tmp.replaceAll("\\s+$", ""));
        }
        return lines;
    }
    public ArrayList<String> prepareReceiptWords(String html) {

        String plainText = Jsoup.parse(html).text();
        String[] words = plainText.split("\\s+");

        if (words.length == 0) {
            return null;
        }

        ArrayList<String> printLines = new ArrayList<String>();
        StringBuffer sb = new StringBuffer(words[0]);
        int len = words.length;
        for (int i = 1; i < len; i++) {
            if (sb.length() + 1 + words[i].length() <= BasePosTextPrinter.PRINTER_MAX_TEXT_LEN) {
                sb.append(" ");
                sb.append(words[i]);
                //flush
                if (i == len - 1){
                    printLines.add(sb.toString());
                    break;
                }
            } else {
                printLines.add(sb.toString());
                sb = new StringBuffer(words[i]);
            }
        }


        return printLines;
    }
    @Override
    protected TaskResult doInBackground() {
        url = getStringArg(ARG_URL);
        beginMarker = getStringArg(ARG_BEGIN_MARKER);
        endMarker = getStringArg(ARG_END_MARKER);

        String html = getHtml(url);
        if (TextUtils.isEmpty(html))
            return failed();
        int beginIndex=0;
        int endIndex = html.length()-1;
        if ( beginMarker != null) {
            beginIndex = html.indexOf(beginMarker);
        }
        if ( endMarker != null) {
            endIndex = html.indexOf(endMarker);
        }

        if (beginIndex < 0 || endIndex < 0 || endIndex < beginIndex)
            return failed();

        html = html.substring(beginIndex, endIndex + (endMarker != null ?endMarker.length():0)); //including markers


        ArrayList<String> printLines = prepareReceiptLineFeed(html);
        if ( printLines == null) {
            return succeeded().add(EXTRA_LINES, new String[0]);
        }
        if (BuildConfig.DEBUG){
            consolePrint(printLines);
        }

        return succeeded().add (EXTRA_LINES, printLines.toArray(new String[printLines.size()]));
    }

    private static String getHtml(String urlS) {
        StringBuffer sb = null;
        BufferedReader in = null;
        try {
            URL url = new URL(urlS);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            sb = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
        } catch (IOException e) {
            Logger.e("ParseHtmlCommand: html getting failed", e);
            return null;
        } finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException ignore) {}
            }
        }

        return sb == null ? null : sb.toString();
    }

    private static void consolePrint(ArrayList<String> printLines) {
        for (String line : printLines){
            for (int i = line.length(); i < BasePosTextPrinter.PRINTER_MAX_TEXT_LEN; i++){
                line = line + " ";
            }
            System.out.println("|" + line + "|");
        }
    }

    public static void start(Context context, String url, String beginMarker, String endMarket, BaseParseHtmlCallback callback) {
        create(ParseHtmlCommand.class)
                .arg(ARG_URL, url)
                .arg(ARG_BEGIN_MARKER, beginMarker)
                .arg(ARG_END_MARKER, endMarket)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseParseHtmlCallback {

        @OnSuccess(ParseHtmlCommand.class)
        public void onSuccess(@Param(EXTRA_LINES) String[] printLines){
            onHtmlParsed(printLines);
        }

        @OnFailure(ParseHtmlCommand.class)
        public void onFailure(){
            onHtmlParseFailure();
        }

        protected abstract void onHtmlParsed(String[] printLines);
        protected abstract void onHtmlParseFailure();
    }

}
