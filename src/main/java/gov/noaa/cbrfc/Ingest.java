package gov.noaa.cbrfc;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.*;
import nl.wldelft.fews.*;

import org.apache.commons.lang3.math.NumberUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;


//@SpringBootApplication
public class Ingest {

	private static final Logger log = LoggerFactory.getLogger(Ingest.class);
	
	@Value("${xml.directory}")
	private String timeSeriesXMLdirectory;

	@Value("${xml.file}")
	private String timeSeriesXMLfile;
	
	@Value("${xml.gz.file}")
	private String timeSeriesXMLgzfile;
	
	@Value("${xml.gzip.filename.extension}")
	private String timeSeriesXMLgzfileExtenion;

	
	@Bean
	public CommandLineRunner demo(LocationRepository locationRepository, 
			                      EnsembleRepository ensembleRepository,
			                      SimulationRepository simulationRepository,
			                      ForecastEventRepository eventsRepository,
			                      ExceedanceRepository exceedanceRepository) {
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
			
			Vector<Simulation> simulations = new Vector<Simulation>();
			
			Vector<Location> locations = new Vector<Location>();
			Vector<Exceedance> exceedances = new Vector<Exceedance>();
			Vector<Ensemble> ensembles = new Vector<Ensemble>();
			Vector<ForecastEvent> forecastEvents = new Vector<ForecastEvent>();
			
			for (String file : list) {
				fileCount++;
				//String temp = new StringBuffer(filename_extension_filter).append(File.separator).append(file).toString();
				log.info("Parsing file #" + fileCount + " :" + file.toString());
				
				Vector<Double> summerFlows = new Vector<Double>(); 

				
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
						  //simulationRepository.save(simulation);
						  simulations.add(simulation);
						  
						  
			        }
			        
			        // set Location with first header record of series
					if (count == 0)
					{
					  
					  
					  location.setId(header.getLocationId());
					  location.setStationName(header.getStationName());
					  location.setPointType("streamflow"); 
					  location.setResponsibleRfc("CBRFC");
					  if (header.getX() != null) location.setX(header.getX());
					  if (header.getY() != null) location.setY(header.getY());
					  if (header.getZ() != null) location.setZ(header.getZ());
					  // save location
					  //locationRepository.save(location);
					  locations.add(location);
					  
					  
	
					  //increment count so as to not repeat again
					  count++;
				    } // end if (count == 0)
					
					// set ensemble for this series element
		        
		        
					Ensemble ensemble = new Ensemble();
					ensemble.setMember(header.getEnsembleMemberIndex().intValue());
					ensemble.setTimeSeriesType(header.getType().value());
					
					//ensembleRepository.save(ensemble);
					ensembles.add(ensemble);
					
					// now all three foreign keys of the join are done (available) for event tags to be processed into forecast events table
					// get all event children of <series>
					
					List events = series.getPropertiesAndDomainAxisValuesAndEvent();
					
					StringBuffer eventsBuffer = new StringBuffer();
					int eventCount = 0;
					double summerFlowTotal = 0.0; boolean flowMissing = false; 
					for (Object eventObj : events) {
						eventCount++;
						EventComplexType event = (EventComplexType) eventObj;
						if (eventCount > 1) eventsBuffer.append(",");
						eventsBuffer.append(event.getValueAttribute());
						if (eventCount == events.size())
						{
						  ForecastEvent forecastEvent = new ForecastEvent();
						  // first the joins
						  forecastEvent.setLocation(location);
						  forecastEvent.setEnsemble(ensemble);
						  forecastEvent.setSimulation(simulation);
						  // add comma-separated values
						  forecastEvent.setFlowData(eventsBuffer.toString());
						  forecastEvents.add(forecastEvent);
						}
						
						// pick up April 1 through July 31
						
						if ((event.getDate().getMonth() >= 4 && event.getDate().getDay() >= 1) && 
						    (event.getDate().getMonth() <= 7 && event.getDate().getDay() <= 31))
                        {
	                       //log.info(event.getDate().toString() + ", Value = " + event.getValueAttribute());
						   double flow = event.getValueAttribute();
						   //Double dFlow = new Double(flow);
						   //if (flow < 0.0) dFlow = Double.NaN; // this works as intended in percentile math, not -999.99!
						   if (flow < 0.0) 
						   {
							   flowMissing = true;
							   continue; 
						   }
						   // add the flows up
						   summerFlowTotal = summerFlowTotal + flow; 
	                       //summerFlows.addElement(dFlow);
                        }
						
						//////////////////
						// 
						//now the timestamp
						//forecastEvent.setFlowTimestamp(ZuluTimeStamp.toInstant(event.getDate(), event.getTime()));
						//forecastEvent.setFlowVolume(event.getValueAttribute());
						//forecastEvent.setFlag(event.getFlag());
						
						// set the missing flag
						//if (forecastEvent.getFlowVolume() == header.getMissVal())
						//{
							//forecastEvent.setMissing(true);
						//}
						
						
						//eventsRepository.save(forecastEvent);
					} // end for (Object eventObj : events)  
					
					if (summerFlowTotal == 0.0) 
					{
						// means none of the flows were numbers 
						summerFlows.addElement(Double.NaN);
					}
					else
					{
						if (flowMissing)
						{
							//log.warn("Almost certainly stats messed up - MISSING FLOWS among non-zeros" );
						}
						summerFlows.addElement(new Double(summerFlowTotal));
					}
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
		        
		        
		        
		        ////////////////////////////////////////////////////////////////////////////////////
		        // compute/set exceedances for location before processing next file (i.e. location)
		        
		        double [] summerFlowArray = new double[summerFlows.size()];
		        //log.info("Size of summerFlowArray = " + summerFlowArray.length);
		        for (int s = 0; s < summerFlows.size(); s++) 
		        {
		        	double raw = ((Double) summerFlows.elementAt(s)).doubleValue();
		        	if (raw == Double.NaN) 
		        	{
		        		summerFlowArray[s] = Double.NaN;	
		        	}
		        	else
		        	{
		        	  double kaf = (raw * 1.983471)/1000;
		        	  //long roundedKaf = Math.round(kaf);
		        	  //summerFlowArray[s] = NumberUtils.toDouble(roundedKaf + "");
		        	  summerFlowArray[s] = kaf;
		        	}
		        }
		        Exceedance exceedance = new Exceedance(summerFlowArray);
		        exceedance.setRunDate(simulation.getSimulationDate());
		        // foreign keys 
		        exceedance.setLocation(location);
		        exceedance.setSimulation(simulation);
		        exceedances.addElement(exceedance);
			} // end for (String file : list)

			
			/////////////////////////////////////////////////////////
			// now do saves of entire collections in one fell swoop
			log.info("Saving " + forecastEvents.size() + " event records");
			simulationRepository.save(simulations);
			locationRepository.save(locations);
			ensembleRepository.save(ensembles);
			eventsRepository.save(forecastEvents);
			exceedanceRepository.save(exceedances);
		}; // end of CommanLineRunner
		
		

		
	}
    
}