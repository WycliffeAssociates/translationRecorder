package org.wycliffeassociates.translationrecorder.questionschunk.tokens;

public class QuestionsToken {
    String q;
    String a;
    String[] ref;

    public String[] getRef() {
        return ref;
    }

    public String getQuestion() {
        return q;
    }

    public String getAnswer() {
        return a;
    }
}
