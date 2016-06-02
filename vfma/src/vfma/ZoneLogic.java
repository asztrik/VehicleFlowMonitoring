package vfma;

import java.util.ArrayList;
import java.util.Iterator;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

public class ZoneLogic {
	
	
	public ArrayList<ZoneChange> calculateChanges(ArrayList<RawSensorData> rsdlist, ArrayList<SensorMap> smlist) {
		
		System.out.println("Calculating changes..." + rsdlist.size());
		
		ArrayList<ZoneChange> zc = new ArrayList<ZoneChange>();
		
		Iterator<RawSensorData> irsd = rsdlist.iterator();
		while(irsd.hasNext()) {
			RawSensorData rsd = irsd.next();
			
			ZoneChange zcelem = new ZoneChange();
			
			Iterator<SensorMap> ism = smlist.iterator();
			while(ism.hasNext()) {
				
				SensorMap sm = ism.next();
				
				if(sm.getSensorId().equals(rsd.getSensorId())) {
					if(rsd.getPassing().equals("0")) {
						zcelem.setZoneId(sm.getLowerZoneId());
						zcelem.setIncrement(-1);
					} else {
						zcelem.setZoneId(sm.getUpperZoneId());
						zcelem.setIncrement(1);
					}
				}
				
			}
			
			System.out.println("Got " + zcelem.getZoneId());
			zc.add(zcelem);
		}
		
		
		
		return zc;
		
	}
	
	
	public void applyChanges(ArrayList<ZoneChange> zc) {
		
		System.out.println("Applying changes...");
		Table table;
		DynamoDB dynamoDB = null;
		try {
		
		AmazonDynamoDBClient client = new AmazonDynamoDBClient()
	            .withEndpoint("https://dynamodb.us-west-2.amazonaws.com");
	    dynamoDB = new DynamoDB(client);
	    table = dynamoDB.getTable("zoneData");
	    
	    System.out.println("Connected!" + zc.size());
	    
	    Iterator<ZoneChange> air = zc.iterator();
	    while(air.hasNext()) {
	    	
	    	
	    	
	    	ZoneChange change = air.next();
	    	
	    	System.out.println("Prepare " + change.getZoneId());
	    	
	    	if(change.getZoneId().equals(null))
	    		if(air.hasNext())
	    			change = air.next();
	    		else
	    			break;
	    	
	    	UpdateItemSpec item = new UpdateItemSpec()
	    			.withPrimaryKey("zone", change.getZoneId())
	    			.withUpdateExpression("add carcount :val1")
	    			.withValueMap(new ValueMap()
	    					.withNumber(":val1", change.getIncrement()  ));
	    			
	    	table.updateItem(item);
	    	
	    	System.out.println("...ok!" );
	    	
	    }
	    
	    
        } catch (Exception e) {
            System.err.println("Unable to update the table:");
            System.err.println(e.getMessage());
        } 
	    
	}

}