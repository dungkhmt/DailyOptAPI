package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ActionEnum {
	// sequence action export order
	public static final String DEPART_FROM_DEPOT = "DEPART_FROM_DEPOT";
	public static final String TAKE_MOOC_AT_DEPOT = "TAKE_AT_MOOC";
	public static final String TAKE_CONTAINER_AT_DEPOT = "TAKE_CONTAINER_AT_DEPOT";
	public static final String WAIT_LOAD_CONTAINER_AT_WAREHOUSE = "WAIT_LOAD_CONTAINER_WAREHOUSE";
	public static final String CARRY_LOADED_CONTAINER_AT_WAREHOUSE = "CARRY_LOADED_CONTAINER_AT_WAREHOUSE";
	public static final String LINK_LOADED_CONTAINER_AT_WAREHOUSE = "LINK_LOADED_CONTAINER_WAREHOUSE";
	public static final String WAIT_RELEASE_LOADED_CONTAINER_AT_PORT = "WAIT_RELEASE_LOADED_CONTAINER_PORT";
	public static final String LINK_EMPTY_MOOC_AT_PORT = "TAKE_EMPTY_MOOC_AT_PORT";
	public static final String RELEASE_MOOC_AT_DEPOT = "RELEASE_MOOC_AT_DEPOT";
	public static final String REST_AT_DEPOT = "REST_AT_DEPOT"; 
	
	// sequence action import order
	public static final String WAIT_LOADED_CONTAINER_AT_PORT = "WAIT_LOADED_CONTAINER_AT_PORT";
	public static final String LINK_LOADED_CONTAINER_AT_PORT = "LINK_LOADED_CONTAINER_AT_PORT";
	public static final String WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE = "WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE";
	public static final String LINK_EMPTY_CONTAINER_AT_WAREHOUSE = "LINK_EMPTY_CONTAINER_AT_WAREHOUSE";
	public static final String RELEASE_CONTAINER_AT_DEPOT = "RELEASE_CONTAINER_AT_DEPOT";
	
	public static final String DELIVERY_CONTAINER = "DELIVERY_CONTAINER";
	public static final String PICKUP_CONTAINER = "PICKUP_CONTAINER";
	
	// extra action
	public static final String UNLINK_EMPTY_CONTAINER_AT_WAREHOUSE = "UNLINK_EMPTY_CONTAINER_AT_WAREHOUSE";
	public static final String UNLINK_LOADED_CONTAINER_AT_WAREHOUSE = "UNLINK_LOADED_CONTAINER_AT_WAREHOUSE";
	
}
