package gov.noaa.cbrfc;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.*;
import nl.wldelft.fews.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;


@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	@Value("${xml.directory}")
	private String timeSeriesXMLdirectory;

	@Value("${xml.file}")
	private String timeSeriesXMLfile;
	
	@Value("${xml.gz.file}")
	private String timeSeriesXMLgzfile;
	
	@Value("${xml.gzip.filename.extension}")
	private String timeSeriesXMLgzfileExtenion;
	
	
	

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	
	@Bean
	public CommandLineRunner demo(LocationRepository locationRepository, 
			                      EnsembleRepository ensembleRepository,
			                      SimulationRepository simulationRepository,
			                      ForecastEventRepository eventsRepository) {
		return (args) -> {
		
			// similarly pick up location from command line, properties etc 
			File dir = new File(timeSeriesXMLdirectory);
			
			if(dir.isDirectory()==false){
				log.error("Directory does not exists : " + timeSeriesXMLdirectory);
				log.info("run the program with property --xml.directory=/pathToXmlsToBeParsed" );
				return;
			}
			
			// look for files with extension SIM24.SQME.24.CS.xml.gz (set by default in application.properties)
			FileExtensionFilter filter = new FileExtensionFilter(timeSeriesXMLgzfileExtenion);	
			
			String [] list;
			if (timeSeriesXMLgzfileExtenion == null || timeSeriesXMLgzfileExtenion.trim().length() == 0)
			{
				log.warn("No file filter criteria provided.  All files in directory will be parsed...");
				list = dir.list();
			}
			else
			{
				log.info("Looking for file names ending with " + timeSeriesXMLgzfileExtenion);
				list = dir.list(filter);
			}
            
			
			//Over-ride list with single file if provided 
			boolean justOneXML = false;
			if (timeSeriesXMLfile != null && timeSeriesXMLfile.trim().length() > 0)
			{
				list = new String[1]; list[0]=timeSeriesXMLfile;justOneXML=true;
			}
			else if (timeSeriesXMLgzfile != null && timeSeriesXMLgzfile.trim().length() > 0)
			{
				list = new String[1]; list[0]=timeSeriesXMLgzfile;
			}
			

			if (list.length == 0) {
				log.warn("Directory " + timeSeriesXMLdirectory + " contains no files to be parsed. Exiting");
				return;
			}

			
			
			int fileCount=0;
			Simulation simulation = new Simulation();
			
			for (String file : list) {
				fileCount++;
				//String temp = new StringBuffer(filename_extension_filter).append(File.separator).append(file).toString();
				log.info("Parsing file #" + fileCount + " :" + file.toString());
				
				
				
				// parse XML files
				
		        JAXBContext jc = JAXBContext.newInstance(TimeSeriesCollectionComplexType.class);
	
		        Unmarshaller unmarshaller = jc.createUnmarshaller();
		        
		        timeSeriesXMLgzfile = file.toString();
		        InputStream is = new GZIPInputStream(new FileInputStream(new File(timeSeriesXMLdirectory, timeSeriesXMLgzfile)) );
		        // just one XML file over-ride
		        if (justOneXML) is = new FileInputStream(new File(timeSeriesXMLdirectory, timeSeriesXMLfile));
		        TimeSeriesCollectionComplexType timeSeriesCollection = 
		        		(TimeSeriesCollectionComplexType) unmarshaller.unmarshal(is);
		        
	            // get all <series> children of root <TimeSeries> 
		        List<TimeSeriesComplexType> timeSeriesList =  timeSeriesCollection.getSeries();

		        
		        

		        int count = 0;
				Location location = new Location();
				
				// for each <series> element under <TimeSeries>
		        for (TimeSeriesComplexType series : timeSeriesList) {
					
					HeaderComplexType header = series.getHeader();
				
					// set simulation info with first header of first file 
			        if (fileCount == 1)
			        {
			        	
						  if (header.getStartDate() != null)
						  {
							  simulation.setStartDate(ZuluTimeStamp.toInstant(header.getStartDate().getDate(), header.getStartDate().getTime()));
							  if (header.getForecastDate() != null)
							  {
							    simulation.setSimulationDate(ZuluTimeStamp.toInstant(header.getForecastDate().getDate(), header.getForecastDate().getTime()));
							  }
							  else
							  {
								  // use start date as simulation date...^_^...sometimes people seem to forget setting this explicitly
								  simulation.setSimulationDate(ZuluTimeStamp.toInstant(header.getStartDate().getDate(), header.getStartDate().getTime()));
							  }
						  }
						  if (header.getEndDate() != null)  simulation.setEndDate(ZuluTimeStamp.toInstant(header.getEndDate().getDate(), header.getEndDate().getTime()));
						  if (header.getTimeStep() != null) 
						  {
							  simulation.setTimeStepMultiplier(header.getTimeStep().getMultiplier().intValue());
							  simulation.setUnits(header.getTimeStep().getUnit().value());
						  }
						  
						  // save forecast/simulation
						  simulationRepository.save(simulation);
						  
			        }
			        
			        // set Location with first header record of series
					if (count == 0)
					{
					  
					  
					  location.setId(header.getLocationId());
					  location.setStationName(header.getStationName());
					  location.setPointType("streamflow"); 
					  location.setResponsibleRfc("CBRFC");
					  location.setX(header.getX());
					  location.setY(header.getY());
					  location.setZ(header.getZ());
					  // save location
					  locationRepository.save(location);
					  
					  
	
					  //increment count so as to not repeat again
					  count++;
				    } // end if (count == 0)
					
					// set ensemble for this series element
		        
		        
					Ensemble ensemble = new Ensemble();
					ensemble.setMember(header.getEnsembleMemberIndex().intValue());
					ensemble.setTimeSeriesType(header.getType().value());
					
					ensembleRepository.save(ensemble);
					
					// now all three foreign keys of the join are done (available) for event tags to be processed into forecast events table
					// get all event children of <series>
					
					List events = series.getPropertiesAndDomainAxisValuesAndEvent();
					
					
					for (Object eventObj : events) {
						EventComplexType event = (EventComplexType) eventObj;
						ForecastEvent forecastEvent = new ForecastEvent();
						// first the joins
						forecastEvent.setLocation(location);
						forecastEvent.setEnsemble(ensemble);
						forecastEvent.setSimulation(simulation);
						//now the timestamp
						forecastEvent.setFlowTimestamp(ZuluTimeStamp.toInstant(event.getDate(), event.getTime()));
						forecastEvent.setFlowVolume(event.getValueAttribute());
						forecastEvent.setFlag(event.getFlag());
						
						// set the missing flag
						if (forecastEvent.getFlowVolume() == header.getMissVal())
						{
							forecastEvent.setMissing(true);
						}
						
						
						eventsRepository.save(forecastEvent);
						
						
						
					} // end for (Object eventObj : events)  
					
					
					////////////////////////////////////////
					//  doing just flow_volumes as an array...in which case ensemble(trace) and simulation can be normalied into one relation. 
					//  This does not work - JPA 2.0 does not support arrays in fields, need to use Collection Mapping - which is basically join
					//  So perhaps try with JDBC template, but that is a purely academic exercise.  
					//  Bottom line, if we are modeling even one column in addition to flow volume (such as flag), we cannot use array
					//  Also, need Spring Data REST, not just Spring Data
					//  So, lets keep it simple for now and ditch the array approach for a future optimization.
					/*
					double [] flowVolumes = new double[events.size()];
					for (int i = 0; i < events.size(); i++) {
						EventComplexType event = (EventComplexType) events.get(i);
						flowVolumes[i] = event.getValueAttribute();
					} // end for (int i = 0; i < events.size(); i++)
				
					ForecastEvent forecastEvent = new ForecastEvent();
					forecastEvent.setLocation(location);
					forecastEvent.setEnsemble(ensemble);
					forecastEvent.setSimulation(simulation);
					forecastEvent.setFlowVolumes(flowVolumes);
					
					//eventsRepository.save(forecastEvent);
	                */
				
		        } // end for (TimeSeriesComplexType series : timeSeriesList)
			} // end for (String file : list)

		};
		
		

		
	}
    
}