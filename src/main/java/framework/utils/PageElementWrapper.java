package framework.utils;

import java.util.List;

public class PageElementWrapper {

    private List<ElementLocator> elements;

    public PageElementWrapper() {
    }

    public List<ElementLocator> getElements() {
        return elements;
    }

    public void setElements(List<ElementLocator> elements) {
        this.elements = elements;
    }
}