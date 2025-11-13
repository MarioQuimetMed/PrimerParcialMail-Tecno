package com.tecnoweb.grupo15sa.utils;

import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;

public class Extractor {

    private static String GMAIL = "d=gmail";
    private static String HOTMAIL = "d=hotmail";
    private static String YAHOO = "d=yahoo";
    private static String UAGRM = "d=uagrm";

    public static Email getEmail(String plain_text) {
        return new Email(getFrom(plain_text), getSubject(plain_text));
    }

    private static String getFrom(String plain_text) {
        String search = "Return-Path: <";
        int index_begin = plain_text.indexOf(search);
        if (index_begin == -1) {
            return "Unknown";
        }
        index_begin += search.length();
        int index_end = plain_text.indexOf(">", index_begin);
        if (index_end == -1) {
            return "Unknown";
        }
        return plain_text.substring(index_begin, index_end);
    }

    private static String getTo(String plain_text) {
        String to = "";
        if (plain_text.contains(GMAIL)) {
            to = getToFromGmail(plain_text);
        } else if (plain_text.contains(HOTMAIL)) {
            to = getToFromHotmail(plain_text);
        } else if (plain_text.contains(YAHOO)) {
            to = getToFromYahoo(plain_text);
        } else if (plain_text.contains(UAGRM)) {
            to = getToFromUagrm(plain_text);
        }
        return to;
    }

    private static String getSubject(String plain_text) {
        String search = "Subject: ";
        int index_begin = plain_text.indexOf(search);
        if (index_begin == -1) {
            return "No Subject";
        }

        index_begin += search.length();
        int index_end = plain_text.indexOf("\n", index_begin);
        if (index_end == -1) {
            return decodeMime(plain_text.substring(index_begin).trim());
        }

        // Construir el subject completo considerando los saltos de línea codificados
        StringBuilder subject = new StringBuilder(plain_text.substring(index_begin, index_end).trim());
        while (true) {
            int next_line_start = index_end + 1;
            if (next_line_start >= plain_text.length()) break;

            int next_line_end = plain_text.indexOf("\n", next_line_start);
            if (next_line_end == -1) next_line_end = plain_text.length();

            String next_line = plain_text.substring(next_line_start, next_line_end).trim();
            if (next_line.isEmpty() || next_line.matches("^[A-Za-z-]+: .*")) {
                break; // Nueva cabecera
            }

            subject.append(" ").append(next_line);
            index_end = next_line_end;
        }

        return decodeMime(subject.toString().trim());
    }

    private static String decodeMime(String encoded) {
        try {
            return MimeUtility.decodeText(encoded);
        } catch (UnsupportedEncodingException e) {
            return encoded;
        }
    }

    private static String getToFromGmail(String plain_text) {
        return getToCommon(plain_text);
    }

    private static String getToFromHotmail(String plain_text) {
        String aux = getToCommon(plain_text);
        return aux.substring(1, aux.length() - 1);
    }

    private static String getToFromYahoo(String plain_text) {
        int index = plain_text.indexOf("To: ");
        if (index == -1) return "Unknown";
        int i = plain_text.indexOf("<", index);
        int e = plain_text.indexOf(">", i);
        if (i == -1 || e == -1) return "Unknown";
        return plain_text.substring(i + 1, e);
    }

    private static String getToFromUagrm(String plain_text) {
        String aux = getToCommon(plain_text);
        return aux.substring(1, aux.length() - 1);
    }

    private static String getToCommon(String plain_text) {
        String aux = "To: ";
        int index_begin = plain_text.indexOf(aux);
        if (index_begin == -1) return "Unknown";
        index_begin += aux.length();
        int index_end = plain_text.indexOf("\n", index_begin);
        if (index_end == -1) return plain_text.substring(index_begin).trim();
        return plain_text.substring(index_begin, index_end).trim();
    }
}
