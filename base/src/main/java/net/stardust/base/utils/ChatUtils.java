package net.stardust.base.utils;

import java.util.regex.Pattern;

public final class ChatUtils {

    public static final Pattern SECTION_PATTERN = Pattern.compile("[§](\\d|[a-fA-Fk-oK-OrR])");

    private ChatUtils() {}

    // Lembrar de fazer método de paginação para listas no chat

    public static String parseColorMessage(String message) {
        StringBuilder formattedMessage = new StringBuilder();
        boolean escapeNext = false;
        for(int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if(c == '\\') {
                escapeNext = true;
                continue;
            }
            if(escapeNext) {
                formattedMessage.append(c == '&' ? c : '\\');
            } else {
                formattedMessage.append(c == '&' ? '§' : c);
            }
            escapeNext = false;
        }
        return formattedMessage.toString();
    }

    public static String removeSections(String message) {
        return SECTION_PATTERN.matcher(message).replaceAll("");
    }

}
