/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DMSTech;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;


public class sequence {

    private canvas[] sequenceItems;

    public String getCity() {
        return city;
    }

    public String getCollection() {
        return collection;
    }

    public String getRepository() {
        return repository;
    }
    private String city;
    private String collection;
    private String repository;
    
    public canvas[] getSequenceItems() {
        return sequenceItems;
    }

    public canvas getSequenceElement(int position) {
        if (sequenceItems.length > position) {
            return sequenceItems[position - 1];
        }
        return null;
    }

    /**build a sequence object given the url of the graph serialization and its format*/
    public sequence(URL[] sequenceUrl, String format, int msID) throws IOException, SQLException {
        city="";
        collection="";
        repository="";
        Model sequenceModel = ModelFactory.createDefaultModel();
        Stack<canvas> accumulator = new Stack();
        int positionCounter = 1;
        //Read all of the urls that were given into a single graph. Usually we just get the manifest,
        //but the imageannotations can also be included
        for (int i = 0; i < sequenceUrl.length; i++) {
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) sequenceUrl[i].openConnection();
            connection.setRequestMethod("GET");
            //this header is for Hopkins, they server based on requested type
            connection.setRequestProperty("accept", "application/n3");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.connect();
            BufferedReader rd = null;
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sequenceModel.read(rd, null, format);
        }
        //this query finds sequence
        String queryString = "prefix dms:<http://dms.stanford.edu/ns/> select ?subject ?predicate WHERE{?subject ?predicate dms:Sequence}";
        
        //Find the image annotation aggregation uri
        String queryString2 = "prefix dms:<http://dms.stanford.edu/ns/> select ?subject ?predicate WHERE{?subject ?predicate dms:ImageAnnotationList}";
        //Find the tei metadata for the manuscript. Items are settlement, repository, and collection+idno (condensed into collection for our purposes)
        String queryString3 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:settlement ?object }";
        String queryString4 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:collection ?object }";
        String queryString5 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:idno ?object }";
        String queryString6 = "prefix tei:<http://www.tei-c.org/ns/1.0/> select ?sub ?object WHERE{ ?sub tei:repository ?object }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, sequenceModel);
        ResultSet results = qe.execSelect();
        String m3UrlString = "";
        //find the uri for the normal sequence
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            m3UrlString = qs.get("subject").toString();
            String resourceQueryString = "prefix ore:<http://www.openarchives.org/ore/terms/> select ?object  WHERE{ <"+m3UrlString+"> ore:isDescribedBy ?object}";
                  query = QueryFactory.create(resourceQueryString);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        //now find where the normal sequence actually resides
        if(results.hasNext())
        {
            qs=results.next();
            m3UrlString=qs.get("object").toString();
        }
  
        }
        query = QueryFactory.create(queryString2);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        String ImgAnnoUrlString = "";
        if (results.hasNext()) {

            QuerySolution qs = results.next();
            //this is the uri of the image annotation list
            ImgAnnoUrlString = qs.get("subject").toString();
            //now find the associated resource
            String resourceQueryString = "prefix ore:<http://www.openarchives.org/ore/terms/> select ?object  WHERE{ <"+ImgAnnoUrlString+"> ore:isDescribedBy ?object}";
            query = QueryFactory.create(resourceQueryString);
            qe =QueryExecutionFactory.create(query, sequenceModel);
            results=qe.execSelect();
            if(results.hasNext())
            {
                qs=results.next();
                ImgAnnoUrlString = qs.get("object").toString();
                
            }
                
        }
        query = QueryFactory.create(queryString3);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            city=qs.get("object").toString();
        }
        query = QueryFactory.create(queryString4);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            collection=qs.get("object").toString();
        }
        
        query = QueryFactory.create(queryString5);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            collection+=" "+qs.get("object").toString();
        }
        
        
        query = QueryFactory.create(queryString6);
        qe = QueryExecutionFactory.create(query, sequenceModel);
        results = qe.execSelect();
        
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            repository=qs.get("object").toString();
        }
        
        
        
        //if a location for the image annotations and the sequence was found, load them into a seperate graph
        if (m3UrlString.compareTo("") != 0 && ImgAnnoUrlString.compareTo("") != 0) {
            URL m3Url = new URL(m3UrlString);
            URL imgAnnoURL = new URL(ImgAnnoUrlString);
            HttpURLConnection m3connection = null;
            m3connection = (HttpURLConnection) m3Url.openConnection();
            m3connection.setRequestMethod("GET");
            m3connection.setRequestProperty("accept", "application/n3");
            m3connection.setDoOutput(true);
            m3connection.setReadTimeout(10000);
            m3connection.connect();
            if(m3UrlString.toLowerCase().endsWith("n3"))
                format="N3";
            else
                format="";
            Model m3Model = ModelFactory.createDefaultModel();
            BufferedReader m3Reader = null;
            m3Reader = new BufferedReader(new InputStreamReader(m3connection.getInputStream()));
            m3Model.read(m3Reader, null, format);
            m3connection = (HttpURLConnection) imgAnnoURL.openConnection();
            m3connection.setRequestMethod("GET");
            m3connection.setRequestProperty("accept", "application/n3");
            m3connection.setDoOutput(true);
            m3connection.setReadTimeout(10000);
            m3connection.connect();
            m3Reader = new BufferedReader(new InputStreamReader(m3connection.getInputStream()));
            sequenceModel = ModelFactory.createDefaultModel();
            if(ImgAnnoUrlString.toLowerCase().endsWith("n3"))
                format="N3";
            else
                format="";
            sequenceModel.read(m3Reader, null, format);
            //find the sequence list (which in jena is a jena list) to get the canvases in order. 
            queryString = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX list: <http://jena.hpl.hp.com/ARQ/list#> select * where{   ?subject list:member ?obj}"; //<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>  ?predicate}";
            query = QueryFactory.create(queryString);
            query = QueryFactory.create(queryString);
            qe = QueryExecutionFactory.create(query, m3Model);
            results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.next();
                //canvas uri
                String canvas = qs.get("obj").toString();
                Resource r = qs.get("obj").asResource();
                //fetch the canvas title
                Query innerQuery = QueryFactory.create("select  ?pred where { <" + r.getURI() + "> " + "<http://purl.org/dc/elements/1.1/title> ?pred}");
                qe = QueryExecutionFactory.create(innerQuery, m3Model);
                ResultSet innerResults = qe.execSelect();
                while (innerResults.hasNext()) {
                    QuerySolution qs2 = innerResults.next();
                    //canvas title
                    String title = qs2.get("pred").toString();
                    //find anything that has the canvas as a target. Should find an image annotation.
                    Query innerQuery3 = QueryFactory.create("select ?sub  where { ?sub <http://www.openannotation.org/ns/hasTarget> <" + r.getURI() + "> }");
                    qe = QueryExecutionFactory.create(innerQuery3, sequenceModel);
                    ResultSet innerResults2 = qe.execSelect();
                    while (innerResults2.hasNext()) {
                        QuerySolution innerqs2 = innerResults2.next();
                        innerqs2.getResource("sub");
                        Query innerQuery4 = QueryFactory.create("select ?pred  where { <" + innerqs2.getResource("sub").getURI() + "> <http://www.openannotation.org/ns/hasBody> ?pred }");
                        qe = QueryExecutionFactory.create(innerQuery4, sequenceModel);
                        ResultSet innerResults4 = qe.execSelect();
                        //the body of the image annotation can be an image or an imagechoice
                        if (innerResults4.hasNext()) {
                            QuerySolution innerqs4 = innerResults4.next();
                            String img = innerqs4.getResource("pred").getURI();
                            //check to see if this is an imagechoice or an imagebody
                            innerQuery4 = QueryFactory.create(" prefix dms:<http://dms.stanford.edu/ns/> select ?pred   where { <" + img + "> ?pred dms:ImageBody }");
                            qe = QueryExecutionFactory.create(innerQuery4, sequenceModel);
                            innerResults4 = qe.execSelect();
                            //it is an imagebody
                            if (innerResults4.hasNext()) {
                                innerqs4 = innerResults4.next();
                                canvas tmp = new canvas(canvas, title, new ImageChoice[]{new ImageChoice(img, 0, 0)}, positionCounter);
                                accumulator.push(tmp);
                                positionCounter++;
                            } else {
                                //this was an imagechoice, query deeper to find all possible images
                                innerQuery4 = QueryFactory.create("prefix dms:<http://dms.stanford.edu/ns/> select ?obj  where {{ <" + img + "> dms:option ?obj }UNION{ <" + img + "> dms:default ?obj }}");
                                qe = QueryExecutionFactory.create(innerQuery4, sequenceModel);
                                innerResults4 = qe.execSelect();
                                //Build an image choice for each image in the options list.
                                Stack<ImageChoice> imgs = new Stack();
                                while (innerResults4.hasNext()) {
                                    innerqs4 = innerResults4.next();
                                    img = innerqs4.getResource("obj").getURI();
                                    imgs.push(new ImageChoice(img, 0, 0));
                                }
                                ImageChoice[] imgAnnos = new ImageChoice[imgs.size()];
                                int ctr = 0;
                                while (!imgs.empty()) {
                                    imgAnnos[ctr] = imgs.pop();
                                    ctr++;
                                }
                                //now build a canvas with all of those images associated
                                canvas tmp = new canvas(canvas, title, imgAnnos, positionCounter);
                                accumulator.push(tmp);
                                positionCounter++;
                            }
                        }
                    }
                }
            }
        }
        sequenceItems = new canvas[accumulator.size()];
        int ctr = 0;
        while (!accumulator.empty()) {
            sequenceItems[ctr] = accumulator.pop();
            ctr++;
        }
    }

    public static void main(String[] args) throws SQLException {
        try {
            URL[] urls = new URL[1];
            urls[0] = new URL("http://dms-data.stanford.edu/BnF/NAF6224/Manifest.xml");
            sequence s=new sequence(urls, "", 1);
            //if city is populated, print shelfmark
            if(s.getCity().compareTo("")!=0)
            System.out.print("Shelfmark:"+s.city+", "+s.repository+", "+s.collection+"\n");
            canvas [] canvases=s.getSequenceItems();
            for(int i=0;i<canvases.length;i++)
            {
                System.out.print("Position:"+canvases[i].getPosition()+"\n");
                System.out.print("Title:"+canvases[i].getTitle()+"\n");
                System.out.print("Canvas:"+canvases[i].getCanvas()+"\n");
                ImageChoice [] images=canvases[i].getImageURL();
                for(int c=0;c<images.length;c++)
                {
                System.out.print("Image:"+images[c].getImageURL()+"\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(sequence.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
