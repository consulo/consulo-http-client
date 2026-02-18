package org.javamaster.httpclient.impl.curl.support;

import org.javamaster.httpclient.impl.curl.enums.QuotesOutside;

import java.util.ArrayList;
import java.util.List;

public class CurlTokenizer {

    private CurlTokenizer() {
    }

    public static List<String> splitInCurlTokens(String curlString) {
        List<String> tokensList = new ArrayList<>();
        int curlStringLength = curlString.length();
        QuotesOutside quotesOutside = QuotesOutside.NONE;
        StringBuilder curToken = new StringBuilder();

        int i = 0;
        while (i < curlStringLength) {
            char c = curlString.charAt(i);
            switch (c) {
                case '"':
                    if (quotesOutside == QuotesOutside.SINGLE) {
                        curToken.append(c);
                    } else if (quotesOutside == QuotesOutside.DOUBLE && curToken.length() == 0) {
                        tokensList.add("");
                    } else {
                        addTokenToList(tokensList, curToken);
                        quotesOutside = updateQuotesState(quotesOutside, '"');
                    }
                    break;

                case '\'':
                    if (quotesOutside == QuotesOutside.DOUBLE) {
                        curToken.append(c);
                    } else if (quotesOutside == QuotesOutside.SINGLE && curToken.length() == 0) {
                        tokensList.add("");
                    } else {
                        addTokenToList(tokensList, curToken);
                        quotesOutside = updateQuotesState(quotesOutside, '\'');
                    }
                    break;

                case '\\':
                    if (curlStringLength <= i + 1 || curlString.charAt(i + 1) != '"' && curlString.charAt(i + 1) != '\'') {
                        curToken.append("\\");
                    } else {
                        curToken.append(curlString.charAt(i + 1));
                        ++i;
                    }
                    break;

                default:
                    if (quotesOutside != QuotesOutside.NONE) {
                        curToken.append(c);
                    } else if (Character.isWhitespace(c)) {
                        addTokenToList(tokensList, curToken);
                    } else {
                        curToken.append(c);
                    }
                    break;
            }

            ++i;
        }

        addTokenToList(tokensList, curToken);

        return tokensList;
    }

    private static QuotesOutside updateQuotesState(QuotesOutside curQuotesState, char curCharacter) {
        if (curQuotesState == QuotesOutside.NONE) {
            if (curCharacter == '"') {
                return QuotesOutside.DOUBLE;
            }

            if (curCharacter == '\'') {
                return QuotesOutside.SINGLE;
            }
        }

        return QuotesOutside.NONE;
    }

    private static void addTokenToList(List<String> tokensList, StringBuilder token) {
        if (token.length() > 0) {
            tokensList.add(token.toString());
            token.setLength(0);
        }
    }
}
