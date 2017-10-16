package org.wycliffeassociates.translationrecorder.project;

import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Version;

/**
 * Created by Joe on 6/27/2017.
 */

public class FileName {

    private static int LANGUAGE =      0b0111111111;
    private static int RESOURCE =      0b1011111111;
    private static int ANTHOLOGY =     0b1101111111;
    private static int VERSION =       0b1110111111;
    private static int BOOK_NUMBER =   0b1111011111;
    private static int BOOK =          0b1111101111;
    private static int CHAPTER =       0b1111110111;
    private static int START_VERSE =   0b1111111011;
    private static int END_VERSE =     0b1111111101;
    private static int TAKE =          0b1111111110;
    private static int MATCH =         0b1111111111;

    private String mPattern;
    String mMask;

    public FileName(Language language, Anthology anthology, Version version, Book book) {
        mMask = anthology.getMask();
        mPattern = computeFileNameFormat(mMask, language, anthology, version, book);
    }

    public String getFileName(int chapter, int ... verses) {
        int mask = Integer.parseInt(mMask, 2);
        StringBuilder sb = new StringBuilder(mPattern);
        if((mask | CHAPTER) == MATCH) {
            sb.append(String.format("c%02d_", chapter));
        }
        if((mask | START_VERSE) == MATCH) {
            sb.append(String.format("v%02d", verses[0]));
        }
        if((mask | END_VERSE) == MATCH) {
            if(verses.length > 1 && verses[0] != verses[1] && verses[1] != -1) {
                sb.append(String.format("-%02d", verses[1]));
            }
        }
        return sb.toString();
    }

    private String computeFileNameFormat(String maskString, Language language, Anthology anthology, Version version, Book book) {
        int mask = Integer.parseInt(maskString, 2);
        StringBuilder sb = new StringBuilder();
        if((mask | LANGUAGE) == MATCH) {
            sb.append(language.getSlug() + "_");
        }
        if((mask | RESOURCE) == MATCH) {
            sb.append(anthology.getResource() + "_");
        }
        if((mask | ANTHOLOGY) == MATCH) {
            sb.append(anthology.getSlug() + "_");
        }
        if((mask | VERSION) == MATCH) {
            sb.append(version.getSlug() + "_");
        }
        if((mask | BOOK_NUMBER) == MATCH) {
            sb.append(String.format("b%02d_", book.getOrder()));
        }
        if((mask | BOOK) == MATCH) {
            sb.append(book.getSlug() + "_");
        }
        mPattern = sb.toString();
        return mPattern;
    }
}
