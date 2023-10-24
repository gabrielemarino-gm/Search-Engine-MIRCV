package it.unipi.aide.model;

import java.util.List;

/**
 * This class represents a preprocessed document
 */
public class Document
{
    public static final long SIZE = 64L + 4L + 4L;

    private String pid;
    private int docid;
    private int tokenCount;
    private List<String> tokens;

    /**
     * Builder class for a Document BEFORE being indexed (created at index creation)
     * @param pid Document's PID
     * @param docid Document ID
     * @param tokens List of Tokens representing the document
     */
    public Document(String pid, int docid, List<String> tokens)
    {
        this.pid = pid;
        this.docid = docid;
        this.tokens = tokens;
        this.tokenCount = tokens.size();
    }

    /**
     * Builder class for a document AFTER being indexed (retrieved during query processing)
     */
    public Document(String pid, int docid, int tokenCount){
        this.pid = pid;
        this.docid = docid;
        this.tokenCount = tokenCount;
    }

    public String getPid() {return pid;}
    public void setPid(String pid) {this.pid = pid;}
    public int getDocid() {return docid;}
    public void setDocid(int docid) {this.docid = docid;}
    public List<String> getTokens() {return tokens;}
    public void setTokens(List<String> tokens) {this.tokens = tokens;}
    public int getTokenCount() {return tokenCount;}
    public void setTokenCount(int tokenCount) {this.tokenCount = tokenCount;}

    @Override
    public String toString() {
        return "Document{" +
                "pid='" + pid + '\'' +
                ", docid=" + docid +
                ", tokenCount=" + tokenCount +
                '}';
    }
}
