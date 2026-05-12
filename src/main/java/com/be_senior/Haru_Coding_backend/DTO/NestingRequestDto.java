package com.be_senior.Haru_Coding_backend.DTO;

public class NestingRequestDto {
    private String code;
    private String language = "java";
    private int maxWidth = 50;
    private int indentSize = 4;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getMaxWidth() { return maxWidth; }
    public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }

    public int getIndentSize() { return indentSize; }
    public void setIndentSize(int indentSize) { this.indentSize = indentSize; }
}
