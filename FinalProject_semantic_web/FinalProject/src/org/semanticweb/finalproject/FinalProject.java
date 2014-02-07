package org.semanticweb.finalproject;

import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class FinalProject {
	static String defaultNameSpace = "http://www.owl-ontologies.com/NYCoil.owl#";
	Model _oil_usuage_energy_benchmark = null;
	Model schema = null;
	public static void main(String[] args) throws IOException
	{
		FinalProject fp = new FinalProject();
		System.out.println("Getting Data from the instance file");
		fp.populateDataFromOwl();
		
		System.out.println("\nPrint max oil consumption for each city \n ");
		System.out.println(" City    Consumption");
		ResultSet response2 = fp.max_oil_consumption(fp._oil_usuage_energy_benchmark);
		if(response2 != null){
			while( response2.hasNext())
			{
				QuerySolution soln = response2.nextSolution();
				Literal city = soln.getLiteral("?city");
				Literal consumption = soln.getLiteral("?consumption");
				System.out.println(city.getString() + "     " + consumption.getString());
	
			}
		}
		
		System.out.println("\n Print the no of residental units its boiler age range and its retirement date\n");
		System.out.println("residential_units  Boiler_age_range  Boiler_retirement_date");
		ResultSet response3 = fp.oil_consumption_energy_benchmark_bbl(fp._oil_usuage_energy_benchmark);
		if(response3 != null){
			while( response3.hasNext())
			{
				QuerySolution soln = response3.nextSolution();
				Literal name = soln.getLiteral("?no_of_residentals");
				Literal number = soln.getLiteral("?age_range");
				Literal list2 = soln.getLiteral("?retire_date");
				System.out.println(name.getString() + "                "+ number.getString()+"             "+ list2.getString());

	
			}
		}
		System.out.println("\nPrint building Boiler and its high low oil consumption for each building\n ");
		System.out.println("Boiler_model   Oil_consumption_high   Oil_consumption_low\n");
		ResultSet response4 = fp.boiler_high_low_consumption(fp._oil_usuage_energy_benchmark);
		if(response4 != null){
			while( response4.hasNext())
			{
				QuerySolution soln = response4.nextSolution();
				Literal name = soln.getLiteral("?list");
				Literal number = soln.getLiteral("?high");
				Literal list2 = soln.getLiteral("?low");
				System.out.println(name.getString() + "             "+ number.getString() + "      " + list2.getString() );
	
			}
		}
		
		System.out.println("\n Print Building name its city and its energy benchmark\n");
		System.out.println("Building_name City Energy_benchmark\n");
		ResultSet response5 = fp.municipal_building_address_energy(fp._oil_usuage_energy_benchmark);
		if(response5 != null){
			while( response5.hasNext())
			{
				QuerySolution soln = response5.nextSolution();
				Literal name = soln.getLiteral("?name");
				Literal number = soln.getLiteral("?city");
				Literal list2 = soln.getLiteral("?energy");
				System.out.println(name.getString() + "         "+ number.getString() + "         " + list2.getString() );
	
			}
		}
		
		System.out.println("\nPrint building manager and owner for each building\n ");
		System.out.println("\n Owner                             Buildingmanager\n");
		ResultSet response1 = fp.building_manager_owner(fp._oil_usuage_energy_benchmark);
		if(response1 != null){
			while( response1.hasNext())
			{
				QuerySolution soln = response1.nextSolution();
				Literal name = soln.getLiteral("?list");
				Literal name2 = soln.getLiteral("?list2");
				System.out.println(name.getString() + "               " + name2.getString());
	
			}
		}
		
	}
	
	private ResultSet municipal_building_address_energy(Model model)
	{
	return runQuery("select DISTINCT ?name ?city ?energy where{"
			+ "?building rdf:type base:municipal_building."
			+ "?building base:agency ?name."
			+ "?building base:source_energy_intensity ?energy."
			+ "?building base:has ?list."
			+ "?list base:city_name ?city} ORDER BY DESC(?energy)", model); 
	}

	
	
	private ResultSet boiler_high_low_consumption(Model model)
	{
	return runQuery("select DISTINCT ?list ?high ?low where{ ?building rdf:type base:Building. "
			+ "?building base:has ?ls."
			+ "?ls base:model ?list."
			+ "?building base:total_oil_consumption_estimation_high ?high."
			+ "?building base:total_oil_consumption_estimation_low ?low.} ORDER BY DESC(?high)", model); 
	}
		
	private ResultSet oil_consumption_energy_benchmark_bbl(Model model)
	{
		return runQuery("select DISTINCT ?age_range ?no_of_residentals ?retire_date where{ "
				+ "?ls rdf:type base:Building."
				+ "?ls base:has ?list."
				+ "?list base:retirement_date ?retire_date."
				+ "?ls base:no_of_residential_units ?no_of_residentals."
				+ "?list base:agerange ?age_range }", model);
	}
	


	private void populateDataFromOwl() throws IOException {
		_oil_usuage_energy_benchmark = ModelFactory.createOntologyModel();
		InputStream oil_usage_energy_instance =
				FileManager.get().open("finalproject.owl");
		_oil_usuage_energy_benchmark.read(oil_usage_energy_instance, defaultNameSpace);
		oil_usage_energy_instance.close();
	}
	
	private ResultSet runQuery(String queryRequest, Model model)
	{
		StringBuffer queryStr = new StringBuffer();
		ResultSet response = null;
		// Establish Prefixes
		//Set default Name space first
		queryStr.append("PREFIX base" + ": <" + defaultNameSpace + "> ");
		queryStr.append("PREFIX rdfs" + ": <" +
		"http://www.w3.org/2000/01/rdf-schema#" + "> ");
		queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
		queryStr.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>");

		//Now add query
		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try
		{
			response = qexec.execSelect();
		}
		finally { }
		
		return response;
	}
	
	
	
	
	
	private ResultSet max_oil_consumption(Model model) {
		return runQuery("select DISTINCT ?city (MAX(?consumption_high) AS ?consumption) where{ "
				+ "?building rdf:type base:Building. "
				+ "?building base:total_oil_consumption_estimation_high ?consumption_high ."
				+ "?building base:has ?list. "
				+ "?address rdf:type base:Facilityaddress."
				+ "?address base:city_name ?city."
				+" FILTER( ?list=?address) } GROUP BY(?city)", model);
	}
	
	
	private ResultSet building_manager_owner(Model model)
	{
	return runQuery("select DISTINCT ?list ?list2 where{ "
			+ "?building rdf:type base:Building. "
			+ "?building base:belongsto ?ls."
			+ "?ls base:name ?list."
			+ "?building base:has ?lt."
			+ "?lt base:name ?list2}", model); 
	}
	

}
