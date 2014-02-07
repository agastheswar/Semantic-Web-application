package org.semweb.assign6;

import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class HelloSemanticWeb {
	static String defaultNameSpace = "http://org.semweb/assign6/people#";
			Model _friends = null;
			Model schema = null;
			InfModel inferredFriends = null;
	public static void main(String[] args) throws IOException
	{
		HelloSemanticWeb hello = new HelloSemanticWeb();
		//Load my FOAF friends
		System.out.println("Load my FOAF Friends");
		hello.populateFOAFFriends();
		System.out.println("\nSay Hello to Myself");
		hello.mySelf(hello._friends);
		// Say Hello to my FOAF Friends
		System.out.println("\nSay Hello to my FOAF Friends");
		hello.myFriends(hello._friends);
		
		
		System.out.println("\nAdd my new friends");
		hello.populateNewFriends();
		
		System.out.println("\nSay hello to all my friends - hey the new ones are missing!");
		hello.myFriends(hello._friends);
		
		// Add the ontologies
		System.out.println("\nAdd the Ontologies");
		hello.populateFOAFSchema();
		hello.populateNewFriendsSchema();
		
		System.out.println("\nSee if the ontologies help to say hello to all my friends - Nope!");
		hello.myFriends(hello._friends);
		
		//Align the ontologies to bind my friends together
		System.out.println("\nOk, lets add alignment statements for the two ontologies.");
		hello.addAlignment();
		//Now say hello to my friends - nope still no new friends!
		System.out.println("\nTry again - Hello to all my friends - nope still not all!");
		hello.myFriends(hello._friends);
		
		//Run reasoner to align the instances
		System.out.println("\nRun a Reasoner");
		hello.bindReasoner();
		
		//Say hello to all my friends
		System.out.println("\nFinally- Hello to all my friends!");
		hello.myFriends(hello.inferredFriends);
		
		//invoke set restriction
		System.out.println("\n Invoke Set Restriction");
		hello.setRestriction(hello.inferredFriends);
		
		System.out.println("\n My Email Friends");
		hello.myEmailFriends(hello.inferredFriends);
		
		System.out.println("\n Run Jena Rule");
		hello.runJenaRule(hello.inferredFriends);
		
		hello.myGmailFriends(hello.inferredFriends);
		
	}
	private void populateFOAFFriends()
	{
		_friends = ModelFactory.createOntologyModel();
		InputStream inFoafInstance =
		FileManager.get().open("FOAFFriends.rdf");
		_friends.read(inFoafInstance,defaultNameSpace);
		try {
			inFoafInstance.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void runJenaRule(Model model)
	{
		String rules = "[emailChange:(?person <http://xmlns.com/foaf/0.1/mbox> ?email)," +
		"strConcat(?email, ?lit), regex( ?lit, '(.*@gmail.com)') ->" +
		"(?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://org.semweb/assign6/people#GmailPerson>)]";
		Reasoner ruleReasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		ruleReasoner = ruleReasoner.bindSchema(schema);
		inferredFriends = ModelFactory.createInfModel(ruleReasoner, model);
	}
	
	public void setRestriction(Model model) throws IOException
	{
		// Load restriction - if entered in model with reasoner,
		// reasoner sets entailments
		InputStream inResInstance = FileManager.get().open("restriction.owl");
		model.read(inResInstance,defaultNameSpace);
		inResInstance.close();
	}
	
	public void myGmailFriends(Model model) {
		runQuery("select DISTINCT ?name where{ ?sub rdf:type <http://org.semweb/assign6/people#GmailPerson> ."+
	"?sub foaf:name ?name } ", model);
	}
	
	public void myEmailFriends(Model model)
	{
	//just get all my email friends only - ones with email
	runQuery(" select DISTINCT ?name where{ ?sub rdf:type <http://org.semweb/assign6/people#EmailPerson> ."+
	"?sub foaf:name ?name} ", model); //add the query string
	}
	
	
	private void addAlignment(){
		// State that :individual is equivalentClass of foaf:Person
		String url = "http://org.semweb/assign6/people#Individual";
		Resource resource = schema.createResource(url);
		Property prop = schema.createProperty("http://www.w3.org/2002/07/owl#equivalentClass");
		Resource obj = schema.createResource("http://xmlns.com/foaf/0.1/Person");
		//schema.add(resource,prop,obj);
		//State that :hasName is an equivalentProperty of foaf:name
		resource = schema.createResource(defaultNameSpace + "hasName");
		prop = schema.createProperty("http://www.w3.org/2002/07/owl#equivalentProperty");
		obj = schema.createResource("http://xmlns.com/foaf/0.1/name");
		schema.add(resource,prop,obj);
		//State that :hasFriend is a subproperty of foaf:knows
		resource = schema.createResource(defaultNameSpace + "hasFriend");
		prop = schema.createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		obj = schema.createResource("http://xmlns.com/foaf/0.1/knows");
		schema.add(resource,prop,obj);
		//State that sem web is the same person as Semantic Web
		resource = schema.createResource("http://org.semweb/assign6/people#me");
		prop = schema.createProperty("http://www.w3.org/2002/07/owl#sameAs");
		obj = schema.createResource("http://org.semweb/assign6/people#I1");
		schema.add(resource,prop,obj);
	}
	
	private void populateNewFriendsSchema() throws IOException
	{
		InputStream inFoafInstance =
		FileManager.get().open("additionalFriends.owl");
		schema.read(inFoafInstance,defaultNameSpace);
		inFoafInstance.close();
	}
	
	private void populateFOAFSchema() throws IOException
	{
		InputStream inFoaf = FileManager.get().open("FOAFFriends.rdf");
		schema = ModelFactory.createOntologyModel();
		//schema.read("http://xmlns.com/foaf/spec/index.rdf");
		// Use local copy for demos without network connection
		schema.read(inFoaf, defaultNameSpace);
		inFoaf.close();
	}
	
	private void bindReasoner()
	{
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		inferredFriends = ModelFactory.createInfModel(reasoner, _friends);
	}
	
	private void populateNewFriends() throws IOException
	{
		InputStream inFoafInstance =
		FileManager.get().open("additionalFriends.owl");
		_friends.read(inFoafInstance,defaultNameSpace);
		inFoafInstance.close();
	}
	
	private void mySelf(Model model)
	{
	//Hello to Me - focused search
		runQuery(" select DISTINCT ?name where { people:me foaf:name ?name}", model); //add the query string
	}
	private void runQuery(String queryRequest, Model model)
	{
		StringBuffer queryStr = new StringBuffer();
		// Establish Prefixes
		//Set default Name space first
		queryStr.append("PREFIX people" + ": <" + defaultNameSpace + "> ");
		queryStr.append("PREFIX rdfs" + ": <" +
		"http://www.w3.org/2000/01/rdf-schema#" + "> ");
		queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
		queryStr.append("PREFIX foaf" + ": <" + "http://xmlns.com/foaf/0.1/"
		+ "> ");
		//Now add query
		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try
		{
			ResultSet response = qexec.execSelect();
			while( response.hasNext())
			{
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?name");
				if( name != null )
				System.out.println( "Hello to " + name.toString() );
				else
				System.out.println("No Friends found!");
			}
		}
		finally { qexec.close();}
	}
	
	private void myFriends(Model model)
	{
	//Hello to just my friends - navigation
	runQuery(" select DISTINCT ?name where{people:me foaf:knows ?friend. ?friend foaf:name ?name } ", model); 
	//add querystring
	}
}
