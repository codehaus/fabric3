package org.fabric3.tests.binding.metro.bookstore;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


@XmlRegistry
public class ObjectFactory {

    private final static QName _SearchResult_QNAME = new QName("urn:bookstore", "searchResult");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: weather
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SearchResult }
     */
    public SearchResult creteSearchResult() {
        return new SearchResult();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WeatherRequest }{@code >}}
     */
    @XmlElementDecl(namespace = "urn:bookstore", name = "searchResult")
    public JAXBElement<SearchResult> createWeatherRequest(SearchResult value) {
        return new JAXBElement<SearchResult>(_SearchResult_QNAME, SearchResult.class, null, value);
    }

}
