package com.czzz.douban;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BookCollectionXmlParser {

	private static final String ns = null;
	private BookCollectionEntry entry;
	// We don't use namespaces

	public List<BookCollectionEntry> parse(InputStream in) throws XmlPullParserException, IOException {
		
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
	
	private List<BookCollectionEntry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<BookCollectionEntry> entries = new ArrayList<BookCollectionEntry>();

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
    private BookCollectionEntry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        
    	entry = new BookCollectionEntry();
    	
    	parser.require(XmlPullParser.START_TAG, ns, "entry");
        
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
            	entry.detail = readTag(parser, "title");
            } else if (name.equals("updated")) {
            	entry.updated = readTag(parser, "updated");
            } else if (name.equals("db:status")){
            	String stau = readTag(parser, "db:status");
            	if(stau.equals("read")){
            		entry.status = "已读";
            	}else if(stau.equals("reading")){
            		entry.status = "在读";
            	}else if(stau.equals("wish")){
            		entry.status = "想读";
            	}
            		
            } else if (name.equals("db:subject")){
            	Log.d("DEBUG", "XML: db:subject");
            	readSubTag(parser, "db:subject");
            } else {
                skip(parser);
            }
        }
        return entry;
    }

    /**
     * Read the text in a tag 
     * @param parser
     * @param tag
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return title;
    }
    
    /**
     * Parse the sub Xml-content between tag, similar to readEntry() method
     * @param parser
     * @param tag
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void readSubTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
    	parser.require(XmlPullParser.START_TAG, ns, tag);
    	while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            
            String name = parser.getName();
            if(name.equals("title")){
            	entry.book.title = readTag(parser, "title");
            }else if(name.equals("link")){
            	readLink(parser);
            }else if(name.equals("db:attribute")){
            	readAttributeTag(parser, "db:attribute");
            }else{
            	skip(parser);
            }
    	}
    	
    }
    
    /**
     * Read tag text with different attribute value
     * @param parser
     * @param tag
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void readAttributeTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
    	
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String mtag = parser.getName();
        String relType = parser.getAttributeValue(null, "name"); //属性
        if (mtag.equals(tag)) {
            if (relType.equals("isbn10")) {
                entry.book.isbn10 = readText(parser);
            } else if (relType.equals("isbn13")){
            	entry.book.isbn13 = readText(parser);
            } else if (relType.equals("author")){
            	if(entry.book.author == null){
        			entry.book.author = readText(parser);
        		}else{
        			entry.book.author += " / " + readText(parser);
        		}
            } else if (relType.equals("translator")){
            	if(entry.book.translator == null){
        			entry.book.translator = readText(parser);
        		}else{
        			entry.book.translator += " / " + readText(parser);
        		}
            } else if (relType.equals("publisher")){
            	entry.book.publisher = readText(parser);
            } else if(relType.equals("price")){
            	entry.book.price = readText(parser);
            } else if(relType.equals("pubdate")){
            	entry.book.pubdate = readText(parser);
            } else{
            	skip(parser);
            	return;
            }
            
        }
        
        parser.require(XmlPullParser.END_TAG, ns, tag);
    	
    } 
    
    /**
     * Read the link with different attribute value
     * @param parser
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel"); //属性
        if (tag.equals("link")) {
            if (relType.equals("image")) {
                entry.book.image = parser.getAttributeValue(null, "href");
                parser.nextTag();
            } else if (relType.equals("alternate")){
//            	entry.book.link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            } else if (relType.equals("mobile")){
//            	entry.book.mobile_link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }else{
            	skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
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
