package com.czzz.douban;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BookCommentXmlParser {

	private static final String ns = null;
	private BookCommentEntry entry;
	// We don't use namespaces

	public List<BookCommentEntry> parse(InputStream in) throws XmlPullParserException, IOException {
		
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}
	
	private List<BookCommentEntry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<BookCommentEntry> entries = new ArrayList<BookCommentEntry>();

        parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

	
	// Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private BookCommentEntry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        
    	entry = new BookCommentEntry();
    	
    	parser.require(XmlPullParser.START_TAG, ns, "entry");
        
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("id")) {
            	String id_link = readTag(parser, "id");
            	entry.id = id_link.substring(
            			id_link.lastIndexOf("/") + 1);
            } else if (name.equals("title")) {
            	entry.title = readTag(parser, "title");
            } else if (name.equals("published")) {
            	entry.published = readTag(parser, "published");
            } else if (name.equals("updated")) {
            	entry.updated = readTag(parser, "updated");
            } else if (name.equals("summary")) {
            	entry.summary = readTag(parser, "summary");
            } else if (name.equals("link")){
            	readLink(parser);
            } else if (name.equals("author")){
            	readSubTag(parser, "author");
            } else if (name.equals("db:votes")){
            	entry.votes = readAttributeTag(parser,"db:votes");
            } else if (name.equals("db:useless")){
            	entry.useless = readAttributeTag(parser,"db:useless");
            } else if (name.equals("gd:rating")){
            	entry.rating = readAttributeTag(parser,"gd:rating");
            } else {
                skip(parser);
            }
        }
        return entry;
    }

    // Processes title tags in the feed.
    private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return title;
    }
    
    private String readSubTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
    	parser.require(XmlPullParser.START_TAG, ns, tag);
    	while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("name")){
            	entry.author_name = readTag(parser, "name");
            }else if(name.equals("link")){	// for author link
            	readLink(parser);
            }else{
            	skip(parser);
            }
    	}
		return null;
    }
    
    //Processes title tags in the feed.
    private String readAttributeTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
    	
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String value = parser.getAttributeValue(null, "value"); //属性
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, ns, tag);
    	
        return value;
    } 
    
    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel"); //属性
        if (tag.equals("link")) {
            if (relType.equals("alternate") && parser.getDepth() == 3) {
                entry.link = parser.getAttributeValue(null, "href");
                parser.nextTag();
        	} else if(relType.equals("alternate") && parser.getDepth() == 4){
        		entry.author_link = parser.getAttributeValue(null, "href");
        		String aLink = entry.author_link.substring(0, entry.author_link.length()-1);
        		entry.author_uid = aLink.substring(aLink.lastIndexOf("/") + 1);
        		parser.nextTag();
        	} else if(relType.equals("icon")){
        		entry.author_avatar = parser.getAttributeValue(null, "href");
        		parser.nextTag();
        	} else{
            	skip(parser);
            	return null;
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
