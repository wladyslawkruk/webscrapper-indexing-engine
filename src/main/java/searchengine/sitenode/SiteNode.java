package searchengine.sitenode;

import java.util.concurrent.CopyOnWriteArrayList;

public class SiteNode {
    private volatile SiteNode parent;

    private String url;
    private volatile CopyOnWriteArrayList<SiteNode> children;


    public SiteNode(String url) {
        this.url = url;
        this.parent = null;
        this.children = new CopyOnWriteArrayList();

    }


    public synchronized boolean addChild(SiteNode element) {
        SiteNode root = this.getRootElement();
        if (!root.contains(element.getUrl())) {
            element.setParent(this);
            this.children.add(element);
            return true;
        }
        return false;
    }

    private boolean contains(String url) {
        if (this.url.equals(url)) {
            return true;
        }
        for (SiteNode child : children) {
            if(child.contains(url))
                return true;
        }

        return false;
    }

    public String getUrl() {
        return this.url;
    }

    private void setParent(SiteNode siteNode) {
        synchronized(this) {
            this.parent = siteNode;
        }
    }

    public SiteNode getRootElement() {
        return this.parent == null ? this : this.parent.getRootElement();
    }

    public CopyOnWriteArrayList<SiteNode> getChildren() {
        return this.children;
    }


}
