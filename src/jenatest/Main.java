package jenatest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

public class Main {
    
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    
    public static void main(String[] args) {
        
        LOGGER.info("Start");
        // testResourceRenaming();
        // testAddStmtIteratorToModel();
        // testEmptyObject();
        // testModifyLiteralString();
        // testLiteralSameValueAs();
        // testLiteralNoLanguage();
        // testNodeGetLiteral();
        // testIteratorToList();
        // testIteratorToListAndForeachRemaining();
        // testAssertionsAndRetractions();
        // testUriChars();
        // testModelRemovesDuplicateTriples();
        // testEmptyIteratorToList();
        // testInfModel();
        // testAddModel();
        testChangeString();

        LOGGER.info("End tests.");
    }
    
    private static void testChangeString() {
        String str = "My string";
        LOGGER.debug("Original string: " + str); // My string
        change(str);
        LOGGER.debug("New string: " + str); // My string
    }
    
    private static void change(String s) {
        s = "a new string";
    }
    
    @SuppressWarnings("unused")
    private static void testAddModel() {
        Model data = ModelFactory.createDefaultModel(); 
        try {
            data.read("rdf/data/102063.nt");
        } catch (RiotException e) {
           e.printStackTrace();
        }
        printModel(data, "original model:");
        
        Model nullModel = null;
        // Can't add null model!
        if (nullModel != null) {
            data.add(nullModel);
        }
        
        Model emptyModel = ModelFactory.createDefaultModel();
        // Can add empty model
        data.add(emptyModel);
        printModel(data, "added empty model");
    }
    
    private static void testInfModel() {

        // Read the data into a model
        Model data = ModelFactory.createDefaultModel(); 
        try {
            data.read("rdf/data/102063.nt");
        } catch (RiotException e) {
           e.printStackTrace();
        }

        String sparql = "CONSTRUCT { ?s ?p ?o } WHERE { " 
                + "?s ?p ?o "
                + "FILTER (?s = <http://draft.ld4l.org/cornell/102063> || "
                + "?o = <http://draft.ld4l.org/cornell/102063>)"
                + " }";
        LOGGER.info(sparql);
        
        Query query = QueryFactory.create(sparql);
        QueryExecution qexec = QueryExecutionFactory.create(
                query, data);
        Model dataModel = qexec.execConstruct(); 
        LOGGER.info("Data model size: " + dataModel.size());
        
        // Read the ontology into a model
        OntModel bfOnt = ModelFactory.createOntologyModel(); 
        try {
            bfOnt.read("http://bibframe.org/vocab/");
        } catch (RiotException e) {
           e.printStackTrace();
        }
        
        Reasoner owlReasoner = ReasonerRegistry.getOWLReasoner();
        InfModel infModel = 
                ModelFactory.createInfModel(owlReasoner, bfOnt, dataModel);
        Model rawModel = infModel.getRawModel();
        LOGGER.info("raw model size: " + rawModel.size());
        LOGGER.info("inf model size: " + infModel.size());
        LOGGER.info("raw model = data model? " + rawModel.isIsomorphicWith(dataModel));
        Resource work = infModel.getResource("http://draft.ld4l.org/cornell/102063");
        Resource workClass = infModel.getResource("http://bibframe.org/vocab/Work");

        LOGGER.info("raw model contains? " + rawModel.contains(work, RDF.type, workClass));
        LOGGER.info("inf model contains? " + infModel.contains(work, RDF.type, workClass));
          
        Resource work1 = ResourceFactory.createResource("http://draft.ld4l.org/cornell/102063");
        Resource workClass1 = ResourceFactory.createResource("http://bibframe.org/vocab/Work");
        LOGGER.info("inf model contains? " + infModel.contains(work1, RDF.type, workClass1));
        
        LOGGER.info("Inference model:");
        printModel(infModel);
     
    }

    private static void testModelRemovesDuplicateTriples() {
        /**
         * Test whether reading in and/or writing out a Jena model removes 
         * duplicate triples.
         * Result: duplicates removed when reading the file into a model.
         */
        String infile = "testinput/input.nt";
        String outfile = "output/output.nt";
        try {
            long incount = 
                    Files.newBufferedReader(Paths.get(infile)).lines().count();
            Model model = ModelFactory.createDefaultModel(); 
            Assert.assertEquals(13, incount);
            model.read(infile);
            Assert.assertEquals(11, model.size());
            FileOutputStream outStream;
            outStream = new FileOutputStream(outfile);
            RDFDataMgr.write(outStream, model, RDFFormat.NTRIPLES);
            long outcount =
                    Files.newBufferedReader(Paths.get(outfile)).lines().count();
            Assert.assertEquals(11, outcount);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    private static void testEmptyIteratorToList() {
        Model model = ModelFactory.createDefaultModel();
        List<Statement> stmts = model.listStatements().toList();
        // No error converting the iterator to a list if the iterator is empty
        for (Statement s : stmts) {
            LOGGER.info("Statement: " + s.toString());
        }

    }

    private static void testUriChars() {
        Model model = ModelFactory.createDefaultModel();
        model.read("test-input/valid-uris.nt");
        printModel(model);
    }
    
    private static void testAssertionsAndRetractions() {
        Model model = ModelFactory.createDefaultModel();
        Model retractions = ModelFactory.createDefaultModel();
        
        Resource mary = model.createResource("http://example.com/people/mary");
        Resource john = model.createResource("http://example.com/people/john");        
        Property property = 
                model.createProperty("http://example.com/vocab/loves"); 
        
        model.add(john, property, mary);
        retractions.add(john, property, mary);
        
        LOGGER.info("Before applying retractions");
        LOGGER.info("model:");
        printModel(model);
        LOGGER.info("retractions:");
        printModel(retractions);
        
        model.remove(retractions);
        
        LOGGER.info("After applying retractions");
       
        LOGGER.info("model:");
        printModel(model);
        LOGGER.info("retractions:");
        printModel(retractions);
        
        Model assertions = ModelFactory.createDefaultModel();
        assertions.add(mary, property, john);
        
        LOGGER.info("Before applying assertions");
        LOGGER.info("model:");
        printModel(model);
        LOGGER.info("assertions:");
        printModel(assertions);
        
        model.add(assertions);
        
        LOGGER.info("After applying assertions");
        LOGGER.info("model:");
        printModel(model);
        LOGGER.info("assertions:");
        printModel(assertions);
        
    }
    
    private static void testIteratorToListAndForeachRemaining() {

        Model model = ModelFactory.createDefaultModel();
        Resource mary = model.createResource("http://example.com/people/mary");
        Resource john = model.createResource("http://example.com/people/john");
        Resource bill = model.createResource("http://example.com/people/bill");
        
        Property property = 
                model.createProperty("http://example.com/vocab/loves");
        
        model.add(mary, property, john);
        model.add(john, property, mary);
        model.add(bill, property, mary);

        StmtIterator it1 = model.listStatements();
        List<Statement> list1 = it1.toList();
        for (Statement s : list1) {
            LOGGER.info(s.toString());           
        }
        LOGGER.info(it1.hasNext() ? "it1 not empty" : "it1 empty");
        
        StmtIterator it2 = model.listStatements();
        List<Statement> list2 = new ArrayList<Statement>();
        it2.forEachRemaining(list2::add);
        for (Statement s : list2) {
            LOGGER.info(s.toString());           
        }
        LOGGER.info(it2.hasNext() ? "it2 not empty" : "it2 empty");
    }

    private static void testIteratorToList() {
        
        Model model = ModelFactory.createDefaultModel();
        Resource mary = model.createResource("http://example.com/people/mary");
        Resource john = model.createResource("http://example.com/people/john");
        Resource bill = model.createResource("http://example.com/people/bill");
        
        Property property = 
                model.createProperty("http://example.com/vocab/loves");
        
        model.add(mary, property, john);
        model.add(john, property, mary);
        model.add(bill, property, mary);

        StmtIterator it = model.listStatements();
        LOGGER.info(it.hasNext() ? "iterator not empty" : "iterator empty");
        printModel(model, "Model before consuming iterator");
        List<Statement> list1 = it.toList(); // This consumes the iterator
        while (it.hasNext()) {
            Statement s = it.nextStatement();
            LOGGER.info("statement " + s.toString());            
        }
        LOGGER.info(it.hasNext() ? "iterator not empty" : "iterator empty");       
        printModel(model, "Model after consuming iterator");
    }

    private static void testNodeGetLiteral() {
        
        Node node = NodeFactory.createLiteral("Hello world", "en");
        LOGGER.info(node.getLiteral()); // Hello world@en
        LOGGER.info(node.getLiteralValue()); // Hello world
        LOGGER.info(node.getLiteralLanguage()); // en

    }

    /*
     * The value of a literal cannot be changed; a new literal has to be 
     * created with a new value. If the value of a literal variable is changed,
     * the model retains the old value; the same is true for a statement created 
     * with the literal.
     */
    private static void testModifyLiteralString() {
        
        Model model = ModelFactory.createDefaultModel();
        Resource subject = model.createResource("http://example.com/people/mary");
        Property property = model.createProperty("http://example.com/vocab/say");
        Literal literal = model.createLiteral("Hello world", "en");
        Statement statement = model.createStatement(subject, property, literal);
        model.add(statement);
        printModel(model); // "Hello world"@en
        String value = literal.getString();
        value = "Goodbye world";
        printModel(model); // "Hello world"@en
        LOGGER.info(literal.getString()); // Hello world
        Literal literal2 = model.createLiteral(value, "en");
        Statement statement2 = model.createStatement(subject, property, literal2);
        // model.remove(statement);
        model.add(statement2);
        printModel(model); // "Goodbye world"@en, "Hello world"@en
        literal = model.createLiteral("Bonjour");
        printModel(model); // "Goodbye world"@en, "Hello world"@en
        LOGGER.info(literal.toString()); // Bonjour
        LOGGER.info(statement.toString()); // "Hello world"@en
       
    }

    private static void testLiteralNoLanguage() {

        String value = "Hello world";
        String language1 = "en";
        String language2 = "";
        String language3 = null;
        
        LOGGER.info("Create node with non-empty language string: OK");
        Node node1 = NodeFactory.createLiteral(value, language1);        
        LOGGER.info("node1: " + node1.toString()); // "Hello world"@en
        LOGGER.info("node1 language: #" + node1.getLiteralLanguage() + "#"); // #en#

        LOGGER.info("Create node with empty language string: OK");
        Node node2 = NodeFactory.createLiteral(value, language2);        
        LOGGER.info("node2: " + node2.toString()); // "Hello world"
        LOGGER.info("node2 language: #" + node2.getLiteralLanguage() + "#"); // ##
        
        LOGGER.info("Create node with null language string: OK");
        Node node3 = NodeFactory.createLiteral(value, language3);        
        LOGGER.info("node3: " + node3.toString()); // "Hello world"
        LOGGER.info("node3 language: #" + node3.getLiteralLanguage() + "#"); // ##

        Model model = ModelFactory.createDefaultModel();

        LOGGER.info("Create literal with non-empty language string: OK");
        Literal literal1 = model.createLiteral(value, language1);        
        LOGGER.info("literal1: " + literal1.toString()); // "Hello world"@en
        LOGGER.info("literal1 language: #" + literal1.getLanguage() + "#"); // #en#

        LOGGER.info("Create literal with empty language string: OK");
        Literal literal2 = model.createLiteral(value, language2);        
        LOGGER.info("literal2: " + literal2.toString()); // "Hello world"
        LOGGER.info("literal2 language: #" + literal2.getLanguage() + "#"); // ##
        
        LOGGER.info("Create literal with null language string: OK");
        Literal literal3 = model.createLiteral(value, language3);        
        LOGGER.info("literal3: " + literal3.toString()); // "Hello world"
        LOGGER.info("literal3 language: #" + literal3.getLanguage() + "#"); // ##
        
    }

    /*
     * Literal.sameValueAs() requires the same string value as well as the same 
     * language or type. String identity alone is not sufficient.
     */
    private static void testLiteralSameValueAs() {
        
        Model model = ModelFactory.createDefaultModel();
        Literal l1 = model.createLiteral("Hello world", "en");
        Literal l2 = model.createLiteral("Bonjour", "fr");
        Literal l3 = model.createLiteral("Bonjour", "fr-ca");
        Literal l4 = model.createLiteral("Hello world");
        Literal l5 = model.createLiteral("Hello world", "en");
        Literal l6 = model.createTypedLiteral("Hello world", XSDDatatype.XSDstring);
        
        LOGGER.info("l1: " + l1.toString());
        LOGGER.info("l2: " + l2.toString());
        LOGGER.info("l3: " + l3.toString());
        LOGGER.info("l4: " + l4.toString());
        LOGGER.info("l5: " + l5.toString());
        LOGGER.info("l6: " + l6.toString());
        LOGGER.info("l1 == l2: " + l1.sameValueAs(l2));
        LOGGER.info("l1 == l3: " + l1.sameValueAs(l3)); 
        LOGGER.info("l2 == l3: " + l2.sameValueAs(l3));
        LOGGER.info("l1 == l4: " + l1.sameValueAs(l4));
        LOGGER.info("l1 == l5: " + l1.sameValueAs(l5));
        LOGGER.info("l1 == l6: " + l1.sameValueAs(l6));
        
        Node n1 = NodeFactory.createLiteral("Hello world", "en");
        Node n2 = NodeFactory.createLiteral("Bonjour", "fr");
        Node n3 = NodeFactory.createLiteral("Bonjour", "fr-ca");
        Node n4 = NodeFactory.createLiteral("Hello world");
        Node n5 = NodeFactory.createLiteral("Hello world", "en");
        Node n6 = NodeFactory.createLiteral("Hello world", XSDDatatype.XSDstring);
        
        LOGGER.info("n1: " + n1.toString());
        LOGGER.info("n2: " + n2.toString());
        LOGGER.info("n3: " + n3.toString());
        LOGGER.info("n4: " + n4.toString());
        LOGGER.info("n5: " + n5.toString());
        LOGGER.info("n6: " + n6.toString());
        LOGGER.info("n1 == n2: " + n1.sameValueAs(n2));
        LOGGER.info("n1 == n3: " + n1.sameValueAs(n3)); 
        LOGGER.info("n2 == n3: " + n2.sameValueAs(n3));
        LOGGER.info("n1 == n4: " + n1.sameValueAs(n4));
        LOGGER.info("n1 == n5: " + n1.sameValueAs(n5));
        LOGGER.info("n1 == n6: " + n1.sameValueAs(n6));
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
        
        LOGGER.info("=================================================");
   
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
            LOGGER.info(file.getName());
            isObjectNull(model);
            LOGGER.info("=================================================");
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
         * NB Turtle input will output invalid Turtle due to the resulting
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
                LOGGER.info(stmt.toString());
                RDFNode object = stmt.getObject();
                if (object == null) {
                    LOGGER.info("Null object");
                } else if (object.isLiteral()) {               
                    LOGGER.info("Literal object: " + object.asLiteral().getLexicalForm());
                } else if (object.isAnon()) {
                    LOGGER.info("Blank node object: " + object.asNode().getBlankNodeLabel());
                } else {
                    String uriString = object.asResource().getURI();
                    LOGGER.info("Resource object: " + uriString);
                    try {
                        URI uri = new URI(uriString);
                        LOGGER.info("URI scheme: " + uri.getScheme());                       
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                }
            }
        }        
    }
    
    private static void printModel(Model model, String msg) {
        
        if (msg != null) {
            LOGGER.info(msg);
        }
        int stmtCount = 0;
        StmtIterator stmts = model.listStatements();
        while (stmts.hasNext()) {
            stmtCount++;
            LOGGER.info(stmtCount + ". " + stmts.next().toString());
        }
    }
    
    private static void printModel(Model model) {
        printModel(model, null);
    }

}
