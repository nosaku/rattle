package com.nosaku.rattle;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class JsonHighlighter {

    // Define patterns for different JSON elements (simplified for illustration)
    private static final Pattern JSON_PATTERN = Pattern.compile(
            "(\"[^\"]*\")|(\\btrue\\b|\\bfalse\\b|\\bnull\\b)|([0-9]+\\.?[0-9]*)|([{}\\[\\]:,])"
    );

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        try {
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(new StringReader(text));
            int lastKwEnd = 0;

            while (parser.nextToken() != null) {
                String styleClass = null;
                switch (parser.getCurrentToken()) {
                    case FIELD_NAME:
                        styleClass = "json-key";
                        break;
                    case VALUE_STRING:
                        styleClass = "json-string";
                        break;
                    case VALUE_NUMBER_INT:
                    case VALUE_NUMBER_FLOAT:
                        styleClass = "json-number";
                        break;
                    case VALUE_TRUE:
                    case VALUE_FALSE:
                        styleClass = "json-boolean";
                        break;
                    case VALUE_NULL:
                        styleClass = "json-null";
                        break;
                    case START_OBJECT:
                    case END_OBJECT:
                    case START_ARRAY:
                    case END_ARRAY:
//                    case COMMA:
//                    case COLON:
                        styleClass = "json-punctuation";
                        break;
                    default:
                        // No specific style for other tokens
                        break;
                }

                if (styleClass != null) {
                    int start = (int) parser.getTokenLocation().getCharOffset();
                    int end = (int) parser.getCurrentLocation().getCharOffset();
                    spansBuilder.add(Collections.emptyList(), start - lastKwEnd);
                    spansBuilder.add(Collections.singleton(styleClass), end - start);
                    lastKwEnd = end;
                }
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

        } catch (IOException e) {
            // Handle parsing errors
            e.printStackTrace();
        }
        return spansBuilder.create();
    }
}
