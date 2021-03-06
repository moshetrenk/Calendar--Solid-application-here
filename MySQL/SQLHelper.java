package cal;


/** This class is used to prevent the GUIs from directly accessing the SQL database
 * that is used for this application. It prevents a lot of repetition and is used
 * as a local instance in any GUI that uses it. This is done so that the path can 
 * be opened only once, even if multiple methods are used. The local instance will
 * call the constructor which opens the path and then the particular methods used
 * will operate using that instance until the exit button is clicked at which point
 * the closePath() method must be called
 *
 * the following block of code should be put into the constructor of any class that 
 * uses an instance of SQLHelper
 *
 *
 * setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 *        addWindowListener(new WindowAdapter() {
 *            public void windowClosing(WindowEvent e) {
 *                help.closePath();
 *                System.exit(1);
 *            }
 *        });
 *
 *
 * use list instead of arraylist?
 *
 */

import java.awt.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class SQLHelper {
	private static Connection connection;
	private static Statement statement;
	private static ResultSet resultSet;
	private static String queryResult;
	private static String url = " ";
	private static String delimiter = "THIS STRING IS THE BETWEEN EVENTS DELIMITER";
	
	public static void main(String[] args){	
		SQLHelper sqlHelper = new SQLHelper();
		
		System.out.println(sqlHelper.getGroupMembersByGroupName("CSC330 Project"));
		//System.out.println(sqlHelper.getCancelledEventNotifications("moshe"));
		/*
		 //if you run this commented block, you'll see that 
		//the user's input will always be encrypted to 32 characters
		 //so the database needs to store 32 char passwords not 16
		  
		StringBuilder a = new StringBuilder();
		
		for(int i = 0; i < Integer.MAX_VALUE / 150; i++)
			a.append(String.valueOf(i));
		
		String b = a.toString();
		
		System.out.println(a);
		System.out.println(help.encrypt(b));
		*/
		
		
	}
	
	SQLHelper(){
	/*
	 * The constructor opens the path to the localhost MySQL database
	 * It also calls the method to turn off safe updates which in turn
	 * allows us to modify elements of the database without identifying
	 * them by their primary key. For example to delete an event, you
	 * need the Date, time, and whose event it is, the primary key is
	 * not relevant
	 */
		//change this to your localhost's username and password
		url = "jdbc:mysql://localhost/thecalendar?useSSL=false&user=root&password=6815";
		
		try {
			connection = DriverManager.getConnection(url);
			statement = connection.createStatement();
			turnOffSafeUpdates();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void closePath(){
		try {
			if(statement != null ) 
				statement.close(); 
			if(connection != null) 
				connection.close(); 
			if(resultSet != null) 
				resultSet.close(); 
		}
		catch(SQLException ex ) {} 
	}
	
	private void turnOffSafeUpdates(){
		try {
			statement.execute("SET SQL_SAFE_UPDATES = 0");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isEventTimeAvailable(ArrayList<Object> obj){
		String user_groupName = (String) obj.get(2), 
				query,
				desiredDateOfEvent  = (String) obj.get(3);
		Integer desiredStartTime = (Integer) obj.get(4),
				desiredEndTime = (Integer) obj.get(5);
		
		
		List list = new ArrayList<Object>();
		
		System.out.println("obj7 = " + obj.get(7));
		System.out.println("obj8 = " + obj.get(8));
/*
		if(!obj.get(7).equals(obj.get(8)))
			query = "select distinct startTimeOfEvent, endTimeofEvent, dateOfEvent, eventNumber "
						+ "from theevent where user_groupID in (select username from calgroup where " 
						+ "groupname = '" + user_groupName + "') or user_groupID in "  
						+ "(select groupName from calgroup where username in "
						+ "(select username from calgroup where groupname = '" + user_groupName + "'))";
		else 
			query = "select distinct startTimeOfEvent, endTimeofEvent, dateOfEvent, eventName " 
				+ "from theevent where user_groupID = '" + user_groupName +"' or user_groupID " 
				+ "in (select groupName from calgroup where username = '" + user_groupName + "')";
*/

		if(!obj.get(7).equals(obj.get(8)))
			query = "select distinct startTimeOfEvent, endTimeofEvent, dateOfEvent, eventNumber "
					+ "from theevent where dateOfEvent = '" + desiredDateOfEvent +"' and ("
					+ "user_groupID in (select username from calgroup where groupname = '"
					+ user_groupName + "')or user_groupID in (select groupName from calgroup "
					+ "where username in (select username from calgroup where groupname = '"
					+ user_groupName + "'))) and ((" + desiredStartTime + " < startTimeOfEvent and " 
					+ desiredEndTime + " > endTimeofEvent) or (" + desiredStartTime +" < startTimeOfEvent "
					+ "and (" + desiredEndTime + "> startTimeOfEvent and " + desiredEndTime + "< endTimeofEvent)) "
					+ "or (" + desiredStartTime + "> startTimeOfEvent and " + desiredEndTime + "< endTimeofEvent) "
					+ "or (" + desiredStartTime + " > startTimeOfEvent and " + desiredStartTime + " < endTimeOfEvent and " + desiredEndTime +"> endTimeofEvent) "	
					+ "or (" + desiredStartTime + "= startTimeOfEvent) "
					+ "or (" + desiredEndTime + " = endTimeofEvent))";
		else
			query = "select distinct startTimeOfEvent, endTimeofEvent, dateOfEvent, eventName " 
					+ "from theevent where dateOfEvent = '" + desiredDateOfEvent +"' and (user_groupID = '" + user_groupName +"' or user_groupID " 
					+ "in (select groupName from calgroup where username = '" + user_groupName + "'))"
					+ "and ((" + desiredStartTime + " < startTimeOfEvent and " 
					+ desiredEndTime + " > endTimeofEvent) or (" + desiredStartTime +" < startTimeOfEvent "
					+ "and (" + desiredEndTime + "> startTimeOfEvent and " + desiredEndTime + "< endTimeofEvent)) "
					+ "or (" + desiredStartTime + "> startTimeOfEvent and " + desiredEndTime + "< endTimeofEvent) "
					+ "or (" + desiredStartTime + " > startTimeOfEvent and " + desiredStartTime + " < endTimeOfEvent and " + desiredEndTime +"> endTimeofEvent) "	
					+ "or (" + desiredStartTime + "= startTimeOfEvent) "
					+ "or (" + desiredEndTime + " = endTimeofEvent))";
		
		System.out.println("query = " + query);
		
		try {
				resultSet = statement.executeQuery(query);

				while(resultSet.next()){
					//	list.add(resultSet.getObject(1));
					for(int i = 1; i <= 4; i++)
						list.add(resultSet.getObject(i));
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		System.out.println("resultSet = " + list);
		System.out.println("list.size = " + list.size());
		
		if(list.size() == 0)
			return true;
		
		return false;
		
	}
	
	public void newGroup(String groupCreator, String groupName){
		String query = "insert calGroup values(" + (getHighestPrimaryKey("calGroup") + 1) + ", '" 
				+ groupCreator + "', '" + groupName + "', 1)";
		System.out.println(query);
		try{
			statement.execute(query);
		}
		catch (SQLException ex){
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	public boolean queryUsernamePassword(String username, String password){
		try {
			resultSet = statement.executeQuery("select userpassword from friend where username = '" + username + "'");
			
			while(resultSet.next())
				queryResult = resultSet.getString(1);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return password.equals(queryResult);
	}
	
	public void insertEvent(ArrayList<?> list){
		//this method is built around the AddEvent screens current simplest
		//option. 
		//TODO Change parameter to Event and have AddEvent do the same
		String q = "insert theEvent values (" + list.get(0) + ", '" 
				+ list.get(1) + "', '" + list.get(2) + "', '" + list.get(3)
				+ "', " + list.get(4) + ", " + list.get (5)+ ", '" 
                + list.get(6)  + "', 1, " + list.get(7) + ")";
		
		System.out.println(q);
		
		try {			
			statement.execute(q);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	
	public static String getUsername(String username){
		//this method is only used when a user is trying to create
		//a new account and the input needs to be verified as a 
		//unique username
		
		try {
			resultSet = statement.executeQuery("select username from thecalendar.friend where username = '" + username +"'");
			
			while(resultSet.next()) {
				queryResult = resultSet.getString(1); 
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		if(queryResult == null){
			return "  ";
		}
		
		return queryResult;
	}
	
	public static String getGroupName(String groupName){
		//this method is only used when a user is trying to create
		//a new group and the input needs to be verified as a 
		//unique group name
		try {
			resultSet = statement.executeQuery("select distinct groupName from thecalendar.calgroup where groupName = '" + groupName +"'");
			
			while(resultSet.next()) {
				queryResult = resultSet.getString(1); 
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		if(queryResult == null){
			return "  ";
		}
		
		return queryResult;
	}
	
	public String query(String query){
		/* This is a generic method that can be used for any query that
		 * will return exactly one field. If the query returns multiple
		 * fields then it will return the first element of the LAST row
		 * Also it assumes that the datatype returned is (or can be 
		 * parsed to) a String.
		 * This method is never used as it would require the class calling
		 * it to have knowledge of the database. Instead, there are 
		 * specific query methods
		 */
		
		try {
			resultSet = statement.executeQuery(query);
			while(resultSet.next()) {
				queryResult = resultSet.getString(1); 
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		if(queryResult == null){
			return "  ";
		}
		
		return queryResult;
	}
	/*
	public void insert(String query){
		//This is a generic insert method, any insert statement can be sent
		//pass an argument such as "insert into friend values ("moshe", "password", "url")
		/* This method is never used as it would require the class calling it to have
		 * knowledge of the database. Instead, there are specific insert methods
		 */
		try {			
			statement.execute(query);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}*/	
	
	//overloaded insert, pass a table and the values to give it
	public void insert(String table, ArrayList<?> values){
		/* This is an even more generic insert method, it only needs a table name
		 * and an ArrayList of the values to insert. Again this method is avoided 
		 * because it requires the class calling it to have knowledge of the database
		 * Specifically, it would have to know the order of the fields of the table. 
		 * This method is designed such that if the input is of type String then it 
		 * will surround it in apostrophes per MySQL's delimiting
		 */
		String query = "insert into " + table + " values (";
	
		for(int i = 0; i < values.size(); i++){
			if (values.get(i).getClass().getSimpleName().equals("String")){
				if(i == values.size() - 1)
					query += "'" + values.get(i) + "')";
				else 
					query += "'" + values.get(i) + "', ";
			}
			else{
				if(i == values.size() - 1)
					query += values.get(i) + ")";
				else 
					query += values.get(i) + ", ";
			}		
		}
		
		//System.out.println("For error-catching reasons:\n" + "Insertion = " + query);
		
		try {			
			statement.execute(query);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	public void insertUsernamePassword(String username, String password){
		try {			
			statement.execute("insert into thecalendar.friend values ('" + username +"', '" + password + "', 'URL')");
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	public void addUserToGroup(String username, String groupName, int a){
		try {			
			statement.execute("insert calgroup values (" + (getHighestPrimaryKey("calGroup") + 1) + ", '" + username + "', '" + groupName + "'," + a + ")");
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}       
	}
	
	public ArrayList<?> getEvents(String userId){
		/* This method creates a generic ArrayList of the data of 
		 * a user's events. We considered directly returning a list
		 * of type Event but we elected not to. This will likely be
		 * changed but for the time being, we prefer it like this
		 */
		
		List list = new ArrayList<Object>();
		
		try {
			resultSet = statement.executeQuery("select * from theevent where user_groupid = '" + userId + "'");
			ResultSetMetaData wayTooMeta = resultSet.getMetaData();
			
			while(resultSet.next()) {
				for(int i = 1; i < wayTooMeta.getColumnCount() + 1; i++){
					if (resultSet.getObject(i).getClass().getSimpleName().equals("String"))
						list.add(resultSet.getString(i));
					else if (resultSet.getObject(i).getClass().getSimpleName().equals("Integer"))
						list.add(resultSet.getInt(i));
					else if (resultSet.getObject(i).getClass().getSimpleName().equals("Time"))
						list.add(resultSet.getTime(i));
				}
				
				//I know there are alternatives to this but for now, this is the simplest one
				list.add(delimiter);
			}	
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}

		return list;
	}
	
	public List getGroups(String username){
		List groups = new List();
		
		try {
			resultSet = statement.executeQuery("select * from calGroup where username = '" + username + "'");
			
			while(resultSet.next()) {
				groups.add(resultSet.getString(3));
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return groups;
	}
	
	public ArrayList<String> getGroupMembersByGroupName(String groupName){
		List members = new ArrayList<String>();

		try {
			resultSet = statement.executeQuery("select * from calGroup where groupName= '" + groupName + "'");
			
			while(resultSet.next()) {
				
				members.add(resultSet.getString(2).toLowerCase());
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		//System.out.println("members = " + members);

		return members;
	}
	
	static ArrayList<String> getTodaysEventNames(String date, String username){
		/* This method allows the profile screen to show a user
		 * any events that they have on the relevant date
		 */
		
		List eventNames= new ArrayList<String>();

		try {
			resultSet = statement.executeQuery("select eventName from theEvent where dateOfEvent = '" + date + "' and user_groupID = '" + username + "'");
			//System.out.println("select eventName from theEvent where dateOfEvent = '" + date + "' and user_groupID = '" + username + "'");
			while(resultSet.next()) {
				eventNames.add(resultSet.getString(1));
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}

		return eventNames;
	}
	
	public int getNumberOfGroupMembers(String groupName){
		try {
			resultSet = statement.executeQuery("select count(*) from calGroup where groupname = '" + groupName + "'");
			
			while(resultSet.next()) {
				return resultSet.getInt(1);
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return -1;
	}

	/*public int getNumberOfElements(String tableName){
		return getHighestPrimaryKey(tableName);
		//don't use this method, use getHighestPrimaryKey(...)
		
		String query = "select count(*) from " + tableName;
		
		try {
			resultSet = statement.executeQuery(query);
			while(resultSet.next()) {
				return resultSet.getInt(1);
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return -1;
	}*/
	
	public int getNumberOfEventsByUser(String username){
		try {
			resultSet = statement.executeQuery("select count(*) from theevent where user_groupID = '" + username + "'");
			while(resultSet.next()) {
				return resultSet.getInt(1);
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return -1;
	}
	
	public int isUserAdmin(String username){
		try {
			resultSet = statement.executeQuery("select * from calgroup where username = '" + username + "'");
			while(resultSet.next()) {
				return resultSet.getInt(4);
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return -1;
	}
	
	public void deleteEvent(String groupName, String date, int time){
		try {			
			statement.execute("delete from theEvent where user_groupID = '" 
					+ groupName + "' and dateOfEvent =  '" 
					+ date + "' and startTimeOfEvent = " + time);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	//needs more testing
	public void deleteCancelledEventNotification(CancelledEventNotification cen){
		String q = "delete from notificationsCancelledEvent where groupName = '" + cen.getGroupName()
			+ "'and recipient = '" + cen.getRecipient() + "' and eventname = '"
			+ cen.getEventName() + "' and dateofevent = '" + cen.getDate() 
			+ "' and startTimeOfEvent = '" + cen.getStartTime() +"'";
		
		System.out.println(q);
		
		try {			
			statement.execute(q);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	//needs to be tested
	public void deleteNotificationGroup(Notification_Group n){
		String q = "delete from notificationsGroup where sender = '" + n.getSender()
		+ "' and recipient = '" + n.getRecipient() 
		+ "' and groupName = '" + n.getGroupName() + "'";
		System.out.println(q);
		try {			
			statement.execute(q);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	//needs to be tested
	public void deleteNotificationEvent(User user, Event event){
		String stoe = event.getStartTime().substring(0, 2) + event.getStartTime().substring(3, 5) + event.getStartTime().substring(6, 8);
		
		String q = "delete from notificationsevent where groupname = '" + event.getId()  
				+ "' and recipient = '" + user.getUsername() + "'and dateofevent = '" 
				+ event.getEventDate() + "' and starttimeofevent = " + stoe;
		
		//System.out.println(q);
		
		try {			
			statement.execute(q);
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	//needs to be tested
	public void insertGroupNotification(Notification_Group gr){
		try{
			statement.execute("insert notificationsGroup values(" 
					+ getHighestPrimaryKey("notificationsGroup") + ", '" + gr.getSender()
					+ "', '" + gr.getRecipient() + "', '" + gr.getGroupName() + "')");
		}
		catch(SQLException ex){
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	//needs to be tested
	public void insertEventNotification(Notification_Event ev){
		try{
			statement.execute("insert notificationsEvent values(" + getHighestPrimaryKey("notificationsEvent")
			+ ", '" + ev.getGroupName() + "', '" + ev.getRecipient() + "', '"
			+ ev.getDate() + "', " + ev.getStartTime() + ", " + ev.getEndTime() + ")");
		} catch (SQLException ex){
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	//needs more testing
	public void insertCancelledEventNotification(CancelledEventNotification cen){
		try{
			statement.execute("insert notificationscancelledevent values (" 
					+ getHighestPrimaryKey("notificationscancelledevent") + ", '"
					+ cen.getGroupName() + "', '" + cen.getRecipient() + "', '"
					+ cen.getEventName() + "', '" + cen.getDate() + "', " 
					+ cen.getStartTime() + ")");
		}catch(SQLException ex){
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	public int getHighestPrimaryKey(String tableName){
		String primaryKey = getPrimaryKeyForAnyTable(tableName);
		
		try {
			resultSet = statement.executeQuery("select " + primaryKey + " from " + tableName 
													+ " order by " + primaryKey + " desc");
			while(resultSet.next()) {
				System.out.println(resultSet.getInt(1));
				return resultSet.getInt(1) + 1;
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return -1;
	}
	
	public ArrayList<Event> getEventNotifications(String username){
	/*this method is overly complicated because the notifications table
	* doesn't store event data so a separate query needs to be made, using the
	* date from the notifications. Basically this method takes in a user's
	* eventNotifications and returns the ArrayList of the corresponding Events. 
	* Easier said than done...
	*/
		
		List notifs = new ArrayList<Event>();
		List temp = new ArrayList<Object>();
		
		try {
			resultSet = statement.executeQuery("select * from notificationsEvent where recipient = '" + username + "'");
		
			while(resultSet.next()) {
				temp.add(resultSet.getString(2));
				temp.add(resultSet.getString(4));
				temp.add(resultSet.getString(5));
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		String[] queries = new String[temp.size() / 3];
		
		for(int a = 0; a < temp.size() / 3; a++){
			queries[a] = "select * from theevent where user_groupID = '" 
					+ temp.get(a * 3) + "' and dateOfEvent = '" + temp.get(a * 3 + 1) 
					+ "' and startTimeOfEvent = " 
					+ temp.get(a * 3 + 2).toString().substring(0, 2) 
					+ temp.get(a * 3 + 2).toString().substring(3, 5) + "00";		
		}
		
		for(int b = 0; b < queries.length; b++){
			List list = new ArrayList<Object>();
			
			try {
				resultSet = statement.executeQuery(queries[b]);
				ResultSetMetaData wayTooMeta = resultSet.getMetaData();
				
				while(resultSet.next()) {
					for(int i = 1; i < wayTooMeta.getColumnCount() + 1; i++){
						if (resultSet.getObject(i).getClass().getSimpleName().equals("String"))
							list.add(resultSet.getString(i));
						else if (resultSet.getObject(i).getClass().getSimpleName().equals("Integer"))
							list.add(resultSet.getInt(i));
						else if (resultSet.getObject(i).getClass().getSimpleName().equals("Time"))
							list.add(resultSet.getTime(i));
					}
					
					Event tempEvent = new Event(list);
					notifs.add(tempEvent);
				}
				
			}
			catch(SQLException ex ) {
				System.err.println("DB Exception: " + ex); 
			}	
		}
		
		return notifs;
	}
	
	//do some more testing on this...
	//the getString() calls may be out of order for certain notifications
	public ArrayList<Notification_Group> getGroupNotifications(String username){
		List sender_and_group = new ArrayList<Notification_Group>();
		
		try {
			resultSet = statement.executeQuery("select * from notificationsGroup where recipient = '" + username + "'");
			while(resultSet.next()) {
				ArrayList<String> ohm = new ArrayList<String>();
				
				System.out.println(resultSet.getString(4));
				ohm.add(resultSet.getString(4));
				System.out.println(resultSet.getString(3));
				ohm.add(resultSet.getString(3));
				System.out.println(resultSet.getString(2));
				ohm.add(resultSet.getString(2));
				
				Notification_Group temp = new Notification_Group(ohm);
				
				sender_and_group.add(temp);
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}

		return sender_and_group;
	}
	
	public ArrayList<CancelledEventNotification> getCancelledEventNotifications(String username){
		//CancelledEventNotification takes two ArrayLists, hence the one and two
		
		List one = new ArrayList<String>(), 
			two = new ArrayList<Object>(), 
			locNotifs = new ArrayList<CancelledEventNotification>();
		
		try {			
			resultSet = statement.executeQuery("select * from notificationsCancelledEvent where recipient = '" + username + "'");
			
			while(resultSet.next()){
				one.add(resultSet.getString(2));
				one.add(resultSet.getString(3));
				two.add(resultSet.getString(4));
				two.add(resultSet.getString(5));
				two.add(resultSet.getString(6));
				locNotifs.add(new CancelledEventNotification(one, two));
				one.clear();
				two.clear();
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return locNotifs;
	}
	
	//needs to be tested
	public void addEventCancelledNotification(CancelledEventNotification cen){
		String q = "insert notificationscancelledevent values (" + getHighestPrimaryKey("notificationscancelledevent")
					+ ", '" + cen.getGroupName() + "', '" + cen.getRecipient() + "', '" 
					+ cen.getEventName() +"', '"+ cen.getDate() + "', " + cen.getStartTime() + ")";
		//System.out.println(q);
		
		try{
			statement.execute(q);
		} 
		catch (SQLException ex){
			System.err.println("DB Exception: " + ex); 
		}
	}

	//needs to be tested
	public void addNotificationGroupRequest(Notification_Group gr){
		try{
			statement.execute("insert notificationsgroup values (" + getHighestPrimaryKey("notificationsgroup") 
						+ ", '" + gr.getSender() + "', '" + gr.getRecipient() + "', '" + gr.getGroupName() + "')");
		}
		catch(SQLException ex){
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	//needs to be tested
	public void addNewEventNotification(Event evt, String recipient){
		System.out.println("we added this event to the database");
		String q = "insert notificationsevent values (" + getHighestPrimaryKey("notificationsevent") 
		+ ", '" + evt.getId() + "', '" + recipient + "', '" + evt.getEventDate() 
		+ "', " + evt.getStartTime() + ", " + evt.getEndTime() + ")";
		
		//System.out.println(q);
		
		try{
			statement.execute(q);
		}
		catch(SQLException ex){
			System.err.println("DB Exception: " + ex); 
		}
	}
	
	public void giveYourApproval(Event evt){
	//to keep track of approved events, when a user accepts an invite,
	//this method is called
	//this needs to be tested	
		String q = "update theevent set approvalsNeeded = " + (evt.getNumberOfAccepted() + 1) + " where eventNumber =" + evt.getEventNumber();
		//System.out.println(q);
		
		try{
			statement.execute(q);
		}
		catch (SQLException ex){
			System.err.println("DB Exception: " + ex);
		}	
	}
	
	public ArrayList<String> getGroupsThatHaveAskedForYourPresence(String username){
		List groups = new ArrayList<String>();

		try {
			resultSet = statement.executeQuery("select * from notificationsGroup where recipient = '" + username + "'");
			
			while(resultSet.next()) {
				groups.add(resultSet.getString(4));
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}

		return groups;
	}
	
	private String getPrimaryKeyForAnyTable(String tableName){
		try {
			resultSet = statement.executeQuery("desc " + tableName);
			while(resultSet.next()) {
				return resultSet.getString(1);
			}
		}
		catch(SQLException ex ) {
			System.err.println("DB Exception: " + ex); 
		}
		
		return " ";
	}
	
	
	//this method was taken from online resources
	//some minor modifications were made 
	//but it is largely not our original code
    public String encrypt(String password) 
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            
            for(int i=0; i< bytes.length; i++)
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
 
            return sb.toString();
            
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }

        return "error";
    }
	
	public String getDelimiter(){
		return delimiter;
	}
}
