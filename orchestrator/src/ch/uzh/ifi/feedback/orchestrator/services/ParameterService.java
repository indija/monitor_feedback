package ch.uzh.ifi.feedback.orchestrator.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import ch.uzh.ifi.feedback.library.rest.Service.IDbService;
import ch.uzh.ifi.feedback.orchestrator.model.FeedbackMechanism;
import ch.uzh.ifi.feedback.orchestrator.model.FeedbackParameter;
import javassist.NotFoundException;

public class ParameterService implements IParameterService{
	
	private ParameterResultParser resultParser;
	
	@Inject
	public ParameterService(ParameterResultParser resultParser) {
		this.resultParser = resultParser;
	}
	
	@Override
	public List<FeedbackParameter> GetAllFor(Connection con, String foreignKeyName, int foreignKey) throws SQLException, NotFoundException
	{
		switch(foreignKeyName){
			case "mechanism_id":
				return GetParametersFor(con, "mechanisms", foreignKeyName, foreignKey);
			case "configuration_id":
				return GetParametersFor(con, "general_configurations", foreignKeyName, foreignKey);
			default:
				throw new NotFoundException("");
		}
	}
	
	private List<FeedbackParameter> GetParametersFor(
			Connection con, 
			String foreignTableName, 
			String foreignKeyName, 
			int foreignKey) throws SQLException
	{
		String sql = String.format(
				  "SELECT p.id, p.parameters_id, p.key, p.value, p.default_value as defaultValue, "
				       + "p.editable_by_user as editableByUser, p.language, p.created_at as createdAt, p.updated_at as updatedAt "
				+ "FROM feedback_orchestrator.parameters as p "
	    		+ "JOIN feedback_orchestrator.%s as f on (p.%s = f.id) "
	    		+ "WHERE f.id = ? ;", foreignTableName, foreignKeyName);
				
	    PreparedStatement s = con.prepareStatement(sql);
	    s.setInt(1, foreignKey);
	    ResultSet result = s.executeQuery();
	    
		Map<Integer, List<FeedbackParameter>> childMap = new HashMap<>();
		Map<FeedbackParameter, Integer> parameterMap = new HashMap<>();
		List<FeedbackParameter> rootParams = new ArrayList<>();
	    while(result.next())
	    {
	    	FeedbackParameter param = new FeedbackParameter();
	    	/*
	    	param.setId(result.getInt("id"));
	    	param.setKey(result.getString("key"));
	    	param.setValue(result.getObject("value"));
	    	param.setDefaultValue(result.getObject("default_value"));
	    	param.setEditableByUser(result.getBoolean("editable_by_user"));
	    	param.setLanguage(result.getString("language"));
	    	param.setCreatedAt(result.getTimestamp("created_at"));
	    	param.setUpdatedAt(result.getTimestamp("updated_at"));
	    	*/
	    	resultParser.SetFields(param, result);
	    	parameterMap.put(param, result.getInt("id"));
	    	
	    	Integer parameterKey = (Integer)result.getObject("parameters_id");
	    	if(parameterKey != null){
	    		if(!childMap.containsKey(parameterKey))
	    			childMap.put(parameterKey, new ArrayList<>());
	    			
	    		childMap.get(parameterKey).add(param);
	    	}else{
	    		rootParams.add(param);
	    	}
	    }
	    
	    return setParametersRecursive(rootParams, parameterMap, childMap);
	}
	
	@Override
	public List<FeedbackParameter> GetAll(
			Connection con) throws SQLException
	{
		String sql = String.format(
				  "Select p.id, p.parameters_id, p.key, p.value, p.default_value as defaultValue, p.editable_by_user as editableByUser, "
				       + "p.language, p.created_at as createdAt, p.updated_at as updatedAt "
				+ "FROM feedback_orchestrator.parameters as p;");
				
	    PreparedStatement s = con.prepareStatement(sql);
	    ResultSet result = s.executeQuery();
	    
		Map<Integer, List<FeedbackParameter>> childMap = new HashMap<>();
		Map<FeedbackParameter, Integer> parameterMap = new HashMap<>();
		List<FeedbackParameter> rootParams = new ArrayList<>();
		
	    while(result.next())
	    {
	    	FeedbackParameter param = new FeedbackParameter();
	    	/*
	    	param.setId(result.getInt("id"));
	    	param.setKey(result.getString("key"));
	    	param.setValue(result.getObject("value"));
	    	param.setDefaultValue(result.getObject("default_value"));
	    	param.setEditableByUser(result.getBoolean("editable_by_user"));
	    	param.setLanguage(result.getString("language"));
	    	param.setCreatedAt(result.getTimestamp("created_at"));
	    	param.setUpdatedAt(result.getTimestamp("updated_at"));
	    	*/
	    	resultParser.SetFields(param, result);
	    	parameterMap.put(param, result.getInt("id"));
	    	
	    	Integer parameterKey = (Integer)result.getObject("parameters_id");
	    	if(parameterKey != null){
	    		if(!childMap.containsKey(parameterKey))
	    			childMap.put(parameterKey, new ArrayList<>());
	    			
	    		childMap.get(parameterKey).add(param);
	    	}else{
	    		rootParams.add(param);
	    	}
	    }
	    
	    return setParametersRecursive(rootParams, parameterMap, childMap);
	}
	
	@Override
	public FeedbackParameter GetById(Connection con, int id) 
			throws SQLException, NotFoundException
	{
		String sql = String.format(
				    "SELECT p.id, p.parameters_id, p.key, p.value, p.default_value as defaultValue, p.editable_by_user as editableByUser, "
				         + "p.language, p.created_at as createdAt, p.updated_at as updatedAt "
				  + "FROM feedback_orchestrator.parameters as p "
	    		  + "WHERE p.id = ? ;");
				
	    PreparedStatement s = con.prepareStatement(sql);
	    s.setInt(1, id);
	    ResultSet result = s.executeQuery();
		
		if(!result.next())
			throw new NotFoundException("parameter with id " + id + "does not exist!/n");
		
		FeedbackParameter param = new FeedbackParameter();
		/*
    	param.setId(result.getInt("id"));
    	param.setKey(result.getString("key"));
    	param.setValue(result.getObject("value"));
    	param.setDefaultValue(result.getObject("default_value"));
    	param.setEditableByUser(result.getBoolean("editable_by_user"));
    	param.setLanguage(result.getString("language"));
    	param.setCreatedAt(result.getTimestamp("created_at"));
    	param.setUpdatedAt(result.getTimestamp("updated_at"));
    	*/
		resultParser.SetFields(param, result);
    	return param;
	}
	
	
	@Override
	public void InsertFor(Connection con, FeedbackParameter param, String foreignKeyName, int foreignKey) throws SQLException, NotFoundException
	{
		switch(foreignKeyName){
			case "mechanism_id":
				InsertParameter(param, null, foreignKey, null, con);
				break;
			case "configuration_id":
				InsertParameter(param, foreignKey, null, null, con);
				break;
			default:
				throw new NotFoundException("");
		}
	}
	
	public void InsertParameter(
			FeedbackParameter param, 
			Integer generalConfigurationId, 
			Integer mechanismId, 
			Integer parameterId, 
			Connection con) throws SQLException, NotFoundException{
		
		PreparedStatement s = con.prepareStatement(
				  "INSERT INTO feedback_orchestrator.parameters "
				+ "(mechanism_id, `key`, value, default_value, editable_by_user, parameters_id, "
				+ "language, general_configurations_id,  created_at) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ;", PreparedStatement.RETURN_GENERATED_KEYS);
		
		s.setObject(1, mechanismId);
		s.setString(2, param.getKey());
		s.setObject(4, param.getDefaultValue());
		s.setObject(5, param.getEditableByUser());
		s.setObject(6, parameterId);
		s.setObject(7, param.getLanguage());
		s.setObject(8, generalConfigurationId);
		s.setTimestamp(9, param.getCreatedAt());
		
		int key;
		if(List.class.isAssignableFrom(param.getValue().getClass())){
			
			s.setObject(3, null);
			s.execute();
		    ResultSet keys = s.getGeneratedKeys();
		    keys.next();
		    key = keys.getInt(1);
		    

		    List<FeedbackParameter> children = (List<FeedbackParameter>)param.getValue();
			for (FeedbackParameter childParam : children){
				InsertParameter(childParam, generalConfigurationId, mechanismId, key, con);	
			}
			
		}else{
			s.setObject(3, param.getValue());
			s.execute();
			
		    ResultSet keys = s.getGeneratedKeys();
		    keys.next();
		    key = keys.getInt(1);
		}
		
		param = GetById(con, key);
	}
	
	@Override
	public void UpdateFor(Connection con, FeedbackParameter param, String foreignKeyName, int foreignKey) throws SQLException, NotFoundException
	{
		switch(foreignKeyName){
			case "mechanism_id":
				UpdateParameter(param, null, foreignKey, null, con);
				break;
			case "configuration_id":
				UpdateParameter(param, null, null, foreignKey, con);
				break;
			default:
				throw new NotFoundException("");
		}
	}
	
	@Override
	public void Update(Connection con, FeedbackParameter param) throws SQLException, NotFoundException
	{
		UpdateParameter(param, null, null, null, con);
	}
	
	public void UpdateParameter(
			FeedbackParameter param, 
			Integer parameterId,
			Integer mechanismId,
			Integer generalConfigurationId,
			Connection con) throws SQLException, NotFoundException{
		
		PreparedStatement s = con.prepareStatement(
				  	"UPDATE feedback_orchestrator.parameters "
				  + "SET mechanism_id = ?, `key` = ?, value = ?, default_value = IFNULL(?, default_value), editable_by_user = IFNULL(?, editable_by_user), "
				       + "parameters_id = ?, language = IFNULL(?, language), general_configurations_id = ? "
				  + "WHERE id = ? ;");
		
		s.setObject(1, mechanismId);
		s.setString(2, param.getKey());
		s.setObject(4, param.getDefaultValue());
		s.setObject(5, param.getEditableByUser());
		s.setObject(6, parameterId);
		s.setObject(7, param.getLanguage());
		s.setObject(8, generalConfigurationId);
		s.setInt(9, param.getId());
		
		if(List.class.isAssignableFrom(param.getValue().getClass())){
			
			s.setObject(3, null);
			s.execute();

		    List<FeedbackParameter> children = (List<FeedbackParameter>)param.getValue();
			for (FeedbackParameter childParam : children){
				if(childParam.getId() == null)
				{
					InsertParameter(childParam, generalConfigurationId, mechanismId, param.getId(), con);
				}else{
					UpdateParameter(childParam, generalConfigurationId, mechanismId, param.getId(), con);		
				}
			}
			
		}else{
			s.setObject(3, param.getValue());
			s.execute();
		}
	}
	
	private List<FeedbackParameter> setParametersRecursive(
			List<FeedbackParameter> params, 
			Map<FeedbackParameter, Integer> parameterMap, 
			Map<Integer, List<FeedbackParameter>> childMap)
	{
		for(FeedbackParameter param : params)
		{
			if(childMap.containsKey(parameterMap.get(param)))
			{
				List<FeedbackParameter> children = childMap.get(parameterMap.get(param));
				param.setValue(setParametersRecursive(children, parameterMap, childMap));
			}
		}
		
		return params;
	}
}