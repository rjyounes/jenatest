package jenatest;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Main {
    
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    
    public static void main(String[] args) {
        
        System.out.println("Start tests.");

        // testResourceRenaming();
        // testAddStmtIteratorToModel();
        testEmptyObject();
        
        System.out.println("End tests.");
    }
    
    private static void testAddStmtIteratorToModel() {
        
        Model model = ModelFactory.createDefaultModel();
        
        Resource mary = model.createResource("http://example.com/data/mary");
        Resource john = model.createResource("http://example.com/data/john");
        Resource bob = model.createResource("http://example.com/data/bob");
        
        Property objProp = 
                model.createProperty("http://example.com/vocab/objProp");
        
        model.add(mary, objProp, bob);
        model.add(mary, objProp, john);
        model.add(john, objProp, bob);
        
        StmtIterator stmts = model.listStatements(mary, null, (RDFNode) null);
        Model newModel = ModelFactory.createDefaultModel();
        newModel.add(stmts);
        printModel(newModel);
        
        
    }

    private static void testResourceRenaming() {
        
        Model model = ModelFactory.createDefaultModel();
        
        Resource oldClass = 
                model.createResource("http://example.com/vocab/oldClass");
        Resource newClass = 
                model.createResource("http://example.com/vocab/newClass");
        Resource classToDelete = 
                model.createResource("http://example.com/vocab/classToDelete");
        Resource superClass = 
                model.createResource("http://example.com/vocab/superClass");
        
        
        Property oldObjProp = 
                model.createProperty("http://example.com/vocab/oldObjProp");
        Property newObjProp = 
                model.createProperty("http://example.com/vocab/newObjProp");
        Property objPropToDelete = model.createProperty(
                    "http://example.com/vocab/objPropToDelete");
        
        Property oldDataProp = 
                model.createProperty("http://example.com/vocab/oldDataProp");
        Property newDataProp = 
                model.createProperty("http://example.com/vocab/newDataProp");
        Property dataPropToDelete = model.createProperty(               
                "http://example.com/vocab/dataPropToDelete");
        
        Resource mary = model.createResource("http://example.com/data/mary");
        Resource john = model.createResource("http://example.com/data/john");
        Resource bob = model.createResource("http://example.com/data/bob");
        
        model.add(oldClass, RDFS.subClassOf, superClass);
        
        model.add(mary, RDF.type, oldClass);
        model.add(mary, RDF.type, classToDelete);
        
        model.add(mary, oldObjProp, john);
        model.add(mary, objPropToDelete, bob);
        model.add(mary, oldDataProp, "Mary");
        model.add(mary, dataPropToDelete, "Wilson");
        
        printModel(model);
        
        /*
         * [http://example.com/data/mary, http://example.com/vocab/oldDataProp, "Mary"]
         * [http://example.com/data/mary, http://example.com/vocab/objPropToDelete, http://example.com/data/bob]
         * [http://example.com/data/mary, http://example.com/vocab/oldObjProp, http://example.com/data/john]
         * [http://example.com/data/mary, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://example.com/vocab/classToDelete]
         * [http://example.com/data/mary, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://example.com/vocab/oldClass]
         */
        
        System.out.println("=================================================");
   
        // Works
        mary = ResourceUtils.renameResource(mary, 
                "http://example.com/data/wilson");
        
        // Doesn't change model
        oldObjProp = (Property) ResourceUtils.renameResource(oldObjProp, 
                newObjProp.getURI());                
        oldDataProp = (Property) ResourceUtils.renameResource(oldDataProp, 
                newDataProp.getURI());
        
        // Changes model but not what we want - changes URI but doesn't change 
        // semantics of the class. In this example, the subclass is still a 
        // subclass of superClass - only the URI has changed.
        oldClass = ResourceUtils.renameResource(oldClass, newClass.getURI());
        
        // Works
        model.removeAll(null, objPropToDelete, (RDFNode) null);
        model.removeAll(null, dataPropToDelete, (RDFNode) null);     
        model.removeAll(null, null, classToDelete);

        
        printModel(model);
        
        /*
         * [http://example.com/data/wilson, http://example.com/vocab/oldDataProp, "Mary"]
         * [http://example.com/data/wilson, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://example.com/vocab/newClass]
         * [http://example.com/data/wilson, http://example.com/vocab/oldObjProp, http://example.com/data/john]
         */
    } 
    
    private static void testEmptyObject() {
        

        for ( File file : new File("test-input/empty-object").listFiles() ) {
            Model model = ModelFactory.createDefaultModel();
            String filename = file.toString();
            model.read(filename);
            System.out.println(file.getName());
            isObjectNull(model);
            System.out.println("=================================================");
        }
        
        /*
         * INPUT/OUTPUT:
         * 
         * 
         * JSON-LD
         * 
         * Input:
         * 
         * "<http://www.loc.gov/mads/rdf/v1#isMemberOfMADSScheme>": 
         *     [
         *         { 
         *             "value" : "",
         *             "type" : "uri"
         *         }
         *     ], 
         *   
         *   
         * Output: 
         * 
         * Syntax errors reported for the jsonld file; not sure how to correct.
         * 72topic11.jsonld
         * 
         * =================================================
         * 
         * N-TRIPLES
         * 
         * Input:
         * 
         * _:bnode131cornell72 <http://www.loc.gov/mads/rdf/v1#isMemberOfMADSScheme> <> . 
         * 
         * 
         * Output: 
         * 
         * 72topic11.nt
         * [79815984c4052867d88535f9530d8039, http://www.loc.gov/mads/rdf/v1#isMemberOfMADSScheme, ]
         * Resource object: 
         * URI scheme: null
         * 
         * =================================================
         * 
         * RDF/XML
         * 
         * Input:
         * 
         * <madsrdf:Authority>
         *     <rdf:type rdf:resource="http://www.loc.gov/mads/rdf/v1#Topic"/>
         *     <madsrdf:authoritativeLabel>1894</madsrdf:authoritativeLabel>
         *     <madsrdf:isMemberOfMADSScheme rdf:resource=""/>
         * </madsrdf:Authority>
         * 
         * 
         * Output:
         * 
         * 72topic11.rdf
         * [-22417409:1511b56f653:-7fff, http://www.loc.gov/mads/rdf/v1#isMemberOfMADSScheme, file:///Users/rjy7/Workspace/jenatest/test-input/empty-object/72topic11.rdf]
         * Resource object: file:///Users/rjy7/Workspace/jenatest/test-input/empty-object/72topic11.rdf
         * URI scheme: file
         * 
         * =================================================
         * 
         * TURTLE
         * 
         * Input:
         * 
         * _:node1a4bq2o58x11 a madsrdf:Authority , madsrdf:Topic ;
         *     madsrdf:authoritativeLabel "1894" ;
         *     madsrdf:isMemberOfMADSScheme <unknown:namespace> .
         *   
         * 
         * Output:
         *     
         * 72topic11.ttl
         * [12bcf3f71ffa4bfac39729bfdb2b0d22, http://www.loc.gov/mads/rdf/v1#isMemberOfMADSScheme, unknown:namespace]
         * Resource object: unknown:namespace
         * URI scheme: unknown
         * 
         * NB Turtle output will create invalid Turtle due to the resulting
         * semi-colon rather than period at the end of the previous line.
         * =================================================
         *
         */
    }
    
    private static void isObjectNull(Model model) {
        StmtIterator stmts = model.listStatements();
        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            if (stmt.getPredicate().getURI().equals("http://www.loc.gov/mads/rdf/v1#isMemberOfMADSScheme")) {
                System.out.println(stmt.toString());
                RDFNode object = stmt.getObject();
                if (object == null) {
                    System.out.println("Null object");
                } else if (object.isLiteral()) {               
                    System.out.println("Literal object: " + object.asLiteral().getLexicalForm());
                } else if (object.isAnon()) {
                    System.out.println("Blank node object: " + object.asNode().getBlankNodeLabel());
                } else {
                    String uriString = object.asResource().getURI();
                    System.out.println("Resource object: " + uriString);
                    try {
                        URI uri = new URI(uriString);
                        System.out.println("URI scheme: " + uri.getScheme());                       
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                }
            }
        }        
    }
    
    private static void printModel(Model model) {
        StmtIterator stmts = model.listStatements();
        while (stmts.hasNext()) {
            System.out.println(stmts.next().toString());
        }
    }

}
