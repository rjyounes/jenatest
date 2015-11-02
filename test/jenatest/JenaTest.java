package jenatest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.Test;

public class JenaTest {

    @Test
    public void testModelRemovesDuplicateTriples() {
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

}
