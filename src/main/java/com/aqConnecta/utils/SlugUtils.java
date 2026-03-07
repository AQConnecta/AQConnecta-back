package com.aqConnecta.utils;

import java.text.Normalizer;

public class SlugUtils {
    /**
     * Cria um slug genérico a partir do `input` fornecido.
     */
    public static String criarSlug(String input) {
        if (input == null) return null;

        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase()
            .replaceAll("[^a-z0-9 ]", "")
            .replace(" ", "-")
            .replaceAll("-+", "-");
    }
}
