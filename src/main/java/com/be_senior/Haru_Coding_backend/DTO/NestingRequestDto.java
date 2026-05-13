package com.be_senior.Haru_Coding_backend.DTO;

public class NestingRequestDto {

    private String code;
    private String language = "java";
    private int screenWidth = 390;
    private int fontSize = 13;
    private int indentSize = 4;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getScreenWidth() { return screenWidth; }
    public void setScreenWidth(int screenWidth) { this.screenWidth = screenWidth; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }

    public int getIndentSize() { return indentSize; }
    public void setIndentSize(int indentSize) { this.indentSize = indentSize; }

    public int calcMaxWidth() {
        return (int) (screenWidth / (fontSize * 0.6));
    }
}
