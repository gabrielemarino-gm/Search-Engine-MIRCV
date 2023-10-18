package it.unipi.aide.model;

import java.util.List;

/**
 * This class represents a preprocessed document
 */
public class ProcessedDocument
{
    private String pid;
    private int docid;
    private int tokenCount;
    private List<String> tokens;

    public ProcessedDocument(String pid, int docid, List<String> tokens)
    {
        this.pid = pid;
        this.docid = docid;
        this.tokenCount = tokens.size();
        this.tokens = tokens;
    }

    public String getPid() {return pid;}
    public void setPid(String pid) {this.pid = pid;}
    public int getDocid() {return docid;}
    public void setDocid(int docid) {this.docid = docid;}
    public List<String> getTokens() {return tokens;}
    public void setTokens(List<String> tokens) {this.tokens = tokens;}
    public int getTokenCount() {return tokenCount;}
    public void setTokenCount(int tokenCount) {this.tokenCount = tokenCount;}

}
