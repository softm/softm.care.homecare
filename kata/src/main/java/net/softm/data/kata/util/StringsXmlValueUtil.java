package net.softm.data.kata.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class StringsXmlValueUtil {
    public static List<String> getValuesNEnValues(Context ctx, String attrName) {
        return getValuesNEnValues(ctx, attrName, Utils.getLanguage(ctx), Utils.getCountry(ctx));
    }

    public static List<String> getValuesNEnValues(Context ctx, String attrName, String language, String country) {
//        ArrayList<StringsXmlValue> values = new ArrayList<>(StringsXmlValueUtil.getValues( ctx, attrName, "en",""));
//        ArrayList<StringsXmlValue> values2 = new ArrayList<>(StringsXmlValueUtil.getValues( ctx, attrName, language,""));
//        ArrayList<StringsXmlValue> values3 = new ArrayList<>(StringsXmlValueUtil.getValues( ctx, attrName, language,country));

        List<String> values = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            values = StringsXmlValueUtil.getValues( ctx, attrName, "en","").stream().map(item->item.value).collect(Collectors.toList());
            List<String> values2 = StringsXmlValueUtil.getValues( ctx, attrName, language,"").stream().map(item->item.value).collect(Collectors.toList());
            List<String> values3 = StringsXmlValueUtil.getValues( ctx, attrName, language,country).stream().map(item->item.value).collect(Collectors.toList());

            values.removeAll(values2);
            values.addAll(values2);
            values.removeAll(values3);
            values.addAll(values3);
        } else {
            ArrayList<StringsXmlValue> values_ = StringsXmlValueUtil.getValues( ctx, attrName, language,"");
            ArrayList<StringsXmlValue> values2_ = StringsXmlValueUtil.getValues( ctx, attrName, language,country);
            ArrayList<StringsXmlValue> values3_ = StringsXmlValueUtil.getValues( ctx, attrName, "en","");
            values_.addAll(values2_);
            values_.addAll(values3_);
            for( StringsXmlValue v : values_ ){
                values.add(v.value);
            }
        }
//        StringsXmlValueUtil.getValues( ctx, attrName, "en","");
//        StringsXmlValueUtil.getValues( ctx, attrName, language,"");
//        StringsXmlValueUtil.getValues( ctx, attrName, language,country);
//        List<StringsXmlValue> rtnValues = values.stream()
//                .distinct()
//                .collect(Collectors.toList());

        return values;
    }


    public static ArrayList<StringsXmlValue> getValues(Context ctx, String attrName, String language, String country) {
        ArrayList<StringsXmlValue> values = new ArrayList<StringsXmlValue>();

        String valuesDir = "res/values";
        if ( language.equals("en") && Utils.isEmpty(country) ) {
//            valuesDir = valuesDir;
        } else {
            if (Utils.isNotEmpty(country) ) {
                valuesDir = valuesDir + "-" + language + "-r" + country;
            } else {
                valuesDir = valuesDir + "-" + language;
            }
        }

        int valuesDirListlen = 0;
        try {
            valuesDirListlen = ctx.getResources().getAssets().list(valuesDir).length;

            Log.d("TAG", "strings.xml values-" + language + (Utils.isNotEmpty(country)?"-r" + country:"") + " : listCount : " + valuesDirListlen);

            if ( valuesDirListlen > 0 ) {


                String v = Utils.loadAssetTextAsString(ctx,valuesDir+"/strings.xml");

                final String regex = "<string name=\"" +attrName+ "\" ?.*>([^<]+)<\\/string>";
//              final String regex = "<string name=\"" +attrName+ "\"(.+\\s*)\\s*?>(.+?)(\\s*)?<\\/string>";
                //   <string name=\"force_stop\"(.+\s*)\s*?>(.+?)(\s*)?<\/string>
                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                final Matcher matcher = pattern.matcher(v);
//                <string name="airplane_mode"(.+\s*)\s*?>(.+?)(\s*)?<\/string>
                while ( matcher.find() ) {
//	        return matcher.group(1);
                    String allAttr = matcher.group(0);
                    String value = matcher.group(1);
                    if ( value.charAt(0) == '"' ) {
                        value = value.substring(1,value.length()-1);
                    }
                    StringsXmlValue ft = new StringsXmlValue(language,country,value);
                    values.add(ft);
                    Log.d("TAG", "findValue['"+ attrName +"'] : " + value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*        */

        return values;
    }

    // ArrayList<FindStringValue>
    public static ArrayList<StringsXmlValue> getValues2(Context ctx, String attrName, String language, String country) {
        ArrayList<StringsXmlValue> values = new ArrayList<StringsXmlValue>();

        String valuesDir = "res/values";
        if ( language.equals("en") && Utils.isEmpty(country) ) {
//            valuesDir = valuesDir;
        } else {
            if (Utils.isNotEmpty(country) ) {
                valuesDir = valuesDir + "-" + language + "-r" + country;
            } else {
                valuesDir = valuesDir + "-" + language;
            }
        }

        int valuesDirListlen = 0;
        try {
            valuesDirListlen = ctx.getResources().getAssets().list(valuesDir).length;

            Log.d("TAG", "strings.xml values-" + language + (Utils.isNotEmpty(country)?"-r" + country:"") + " : listCount : " + valuesDirListlen);

            if ( valuesDirListlen > 0 ) {
// strings.xml
//                String attrName = "force_stop";
                try {
//          Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream(v.getBytes()));
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( ctx.getResources().getAssets().open(valuesDir+"/strings.xml") );
                    //XPath 선언
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    NodeList nodeList = null;
                    try {
                        XPathExpression expr = xPath.compile("/resources/string[@name='" + attrName + "']");
                        nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
//                node = (Node) xPath.evaluate("/resources/string[@name='" + attrName + "']",document, XPathConstants.NODE);
                    } catch (XPathExpressionException e) {
                        throw new RuntimeException(e);
                    }
//      Node node = (Node) xPath.evaluate("//book/title",document, XPathConstants.NODE);
                    for ( int i=0;i<nodeList.getLength();i++ ) {
                        StringsXmlValue ft = new StringsXmlValue(language,country,nodeList.item(i).getTextContent());
//                rNodeList.add(nodeList.item(i).getTextContent());
                        values.add(ft);
                        Log.d("TAG", "findValue['"+ attrName +"'] : " + nodeList.item(i).getTextContent());
                    }
                    // W :
                    // 언어 매핑해서 values-[lng] 연결.
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*        */

        return values;
    }
}


class StringsXmlValue {
    String language;
    String country;
    String value;

    public StringsXmlValue(String language, String country, String value) {
        this.language = language;
        this.country = country;
        this.value = value;
    }
}

