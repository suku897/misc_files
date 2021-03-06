/*
 * @category DeviceManagement
 * @copyright Copyright (C) 2018 Contus. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.tcl.devices.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.tcl.devices.dto.AssignDeviceRequestDto;
import com.tcl.devices.dto.CreateDeviceResponseDto;
import com.tcl.devices.dto.DeviceDeleteRequestDto;
import com.tcl.devices.dto.DeviceHistoryResponseDto;
import com.tcl.devices.dto.DeviceResponseDto;
import com.tcl.devices.dto.DeviceStatusResponseDto;
import com.tcl.devices.dto.DeviceStatusUpdateDto;
import com.tcl.devices.dto.DeviceUpdateRequestDto;
import com.tcl.devices.dto.DeviceUpdateResponseDto;
import com.tcl.devices.dto.NewDeviceRequestDto;
import com.tcl.devices.exception.DeviceAlreadyPresent;
import com.tcl.devices.model.AssignDevice;
import com.tcl.devices.model.Device;
import com.tcl.devices.model.DeviceHistory;
import com.tcl.devices.model.DeviceStatus;
import com.tcl.devices.repository.DeviceAssignmentRepository;
import com.tcl.devices.repository.DeviceHistoryRepository;
import com.tcl.devices.repository.DeviceRepositiry;
import com.tcl.devices.repository.DeviceStatusRepository;
import com.tcl.devices.response.ListResponse;
import com.tcl.devices.response.Response;
import com.tcl.devices.util.DeviceConstants;
import com.tcl.devices.util.Helper;

/*
 * This service contains all the logical functionalities thats implemented in this API
 */
@Service
public class DeviceServiceImp implements DeviceService {

	/*
	 * dependency injection and object initializtion using Autowired annotation
	 */
	@Autowired
	DeviceStatusRepository deviceStatusRepository;

	/*
	 * dependency injection and object initializtion using Autowired annotation
	 */
	@Autowired
	DeviceRepositiry deviceRepository;

	/*
	 * dependency injection and object initializtion using Autowired annotation
	 */
	@Autowired
	private ModelMapper modelMapper;

	/*
	 * dependency injection and object initializtion using Autowired annotation
	 */
	@Autowired
	private DeviceAssignmentRepository deviceAssignmentRepository;

	/*
	 * dependency injection and object initializtion using Autowired annotation
	 */
	@Autowired
	private DeviceHistoryRepository deviceHistoryRepository;

	/*
	 * API for creating a new AMR device. (1). The variables and the fields required
	 * for the API needs to be specified. (2). The base classes(DTO) needed for the
	 * micro-service must be created. (3). Once they're created, the logic for
	 * creating a device record in the DB must be implemented. Logic: a. The request
	 * for creating a device must be received from the front end and must be stored
	 * in the request DTO. It should contain fields like device serialNo, device
	 * name, manufacturer name, etc.,
	 * 
	 * b. The serial no. of the device should be used to verify and validate if the
	 * same device is listed in the DB previously.
	 * 
	 * c. If there is no instance of the device is found in the DB, the device
	 * details are added.
	 * 
	 * d. A unique Id for the device must be created and is stored with the device
	 * details. This unique ID is generated by incrementing the previous device's ID
	 * and prefixing it with the string "DV-".
	 * 
	 * e. A response instance is created and is inserted with details like status of
	 * creation, error code (if any), etc. Then it is sent as a response to the
	 * front end.
	 */
	@Override
	public Response<CreateDeviceResponseDto> addDevice(NewDeviceRequestDto request) {
		Optional<Device> device = deviceRepository.findBySerialNumber(request.getSerialNumber());
		Response<CreateDeviceResponseDto> response = new Response<>();
		CreateDeviceResponseDto createResponse = new CreateDeviceResponseDto();
		if (device.isPresent())
			throw new DeviceAlreadyPresent(DeviceConstants.DEVICE_ALREADY_PRESENT);
		Device newDevice = addNewDevice(request);
		createResponse.setDeviceId(newDevice.getDeviceId());
		response.setData(createResponse);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setError(false);
		response.setMessage(DeviceConstants.DEVICE_ADDED_SUCCESSFULY);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#updateDevice(com.tcl.devices.dto.
	 * DeviceUpdateRequestDto) This method splits the functionalities of the
	 * addDevice method It is used to generate a unique ID for the device and prefix
	 * it with a String that is mentioned in the device constants and saves the
	 * method and then sends a response to the controller which in turn sends a
	 * response to the front end
	 */
	private Device addNewDevice(NewDeviceRequestDto request) {

		String deviceId;
		Optional<Device> device = deviceRepository.findFirstByOrderByCreatedDateDesc();
		if (!device.isPresent())
			deviceId = Helper.generateNewDeviceEui();
		else {
			deviceId = Helper.generateNextDeviceEui(device.get().getDeviceId());
		}
		Optional<DeviceStatus> deviceStatus = deviceStatusRepository.findByStatusIdAndIsActive(request.getStatus(),
				true);
		if (!deviceStatus.isPresent()) {
			throw new DeviceAlreadyPresent(DeviceConstants.INVALID_DEVICE_STATUSID);
		}
		Device newDevice = new Device();
		newDevice.setDeviceName(request.getDeviceName());
		newDevice.setSerialNumber(request.getSerialNumber());
		newDevice.setHardwareVersion(request.getHardwareVersion());
		newDevice.setFirmwareVersion(request.getFirmwareVersion());
		newDevice.setManufacturerName(request.getManufacturerName());
		newDevice.setStatus(request.getStatus());
		newDevice.setCreatedDate(new Date());
		newDevice.setModifiedDate(new Date());
		newDevice.setCreatedBy(request.getUserId());
		newDevice.setModifiedBy(request.getUserId());
		newDevice.setDeviceEui(request.getDeviceEui());
		newDevice.setDeviceId(deviceId);
		newDevice.setModelNumber(request.getModelNumber());
		if ((request.getCustomerId() != null))
			newDevice.setCustomerId(request.getCustomerId());
		newDevice.setIsActive(true);
		newDevice.setDeviceEui(request.getDeviceEui());
		return deviceRepository.save(newDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#updateDevice(com.tcl.devices.dto.
	 * DeviceUpdateRequestDto) (1). DeviceId and the update details are received
	 * from the request. (2). The deviceId is used to verify if any device with the
	 * specified deviceID is present in the DB. (3). If the device is present, the
	 * device data get updated. Else an error (specifying that there is no device
	 * present with that deviceID) is sent in the response. (4). Then the status of
	 * the update process, with the error code(if any) is sent in the response to
	 * the front end.
	 */

	@Override
	public Response<DeviceUpdateResponseDto> updateDevice(DeviceUpdateRequestDto request) {
		Response<DeviceUpdateResponseDto> response = new Response<>();
		DeviceUpdateResponseDto deviceResponse = new DeviceUpdateResponseDto();
		Optional<Device> device = deviceRepository.findByDeviceIdAndIsActive(request.getDeviceId(), true);

		if (device.isPresent()) {
			Optional<DeviceStatus> deviceStatus = deviceStatusRepository.findByStatusIdAndIsActive(request.getStatus(),
					true);
			if (!deviceStatus.isPresent())
				throw new DeviceAlreadyPresent(DeviceConstants.INVALID_DEVICE_STATUSID);
			Device bufferDevice = device.get();
			Device updateDevice = bufferDevice;
			updateDevice = modelMapper.map(request, Device.class);
			updateDevice.setModifiedBy(request.getUserId());
			updateDevice.setModifiedDate(new Date());
			updateDevice.setCustomerId(bufferDevice.getCustomerId());
			updateDevice.setCreatedBy(bufferDevice.getCreatedBy());
			updateDevice.setCreatedDate(bufferDevice.getCreatedDate());
			updateDevice.setIsActive(bufferDevice.getIsActive());
			updateDevice.setStatus(request.getStatus());
			deviceRepository.save(updateDevice);
			addDeviceHistory(request.getUserId(), request.getDeviceId(), request.getStatus());
			deviceResponse.setDeviceId(updateDevice.getDeviceId());
			response.setData(deviceResponse);
			response.setError(false);
			response.setMessage(DeviceConstants.DEVICE_UPDATED_SUCCESSFULLY);
			response.setStatus(HttpServletResponse.SC_OK);
			return response;
		} else {
			response.setError(true);
			response.setMessage(DeviceConstants.INVALID_DEVICE_ID);
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			return response;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#updateDevice(com.tcl.devices.dto.
	 * DeviceUpdateRequestDto) (1). DeviceId and the update details are received
	 * from the request. (2). The deviceId is used to verify if any device with the
	 * specified deviceID is present in the DB. (3). If the device is present, the
	 * device data get updated. Else an error (specifying that there is no device
	 * present with that deviceID) is sent in the response. (4). Then the status of
	 * the update process, with the error code(if any) is sent in the response to
	 * the front end.
	 */
	@Override
	public Response<Boolean> updateDeviceStatus(DeviceStatusUpdateDto request) {
		Optional<DeviceStatus> deviceStatus = deviceStatusRepository.findByStatusIdAndIsActive(request.getStatus(),
				true);
		if (!deviceStatus.isPresent())
			throw new DeviceAlreadyPresent(DeviceConstants.INVALID_DEVICE_STATUSID);
		Response<Boolean> response = new Response<>();
		List<Device> device = deviceRepository.findByDeviceIdInAndIsActive(request.getDeviceId(), true);
		if (!device.isEmpty()) {
			List<DeviceHistory> deviceHistory = new ArrayList<>();
			DeviceHistory updateDeviceHistory = new DeviceHistory();
			for (Device saveDevice : device) {
				saveDevice.setStatus(request.getStatus());
				saveDevice.setModifiedDate(new Date());
				saveDevice.setModifiedBy(request.getUserId());
				updateDeviceHistory.setDeviceId(saveDevice.getDeviceId());
				updateDeviceHistory.setStatus(request.getStatus());
				updateDeviceHistory.setModifiedBy(request.getUserId());
				updateDeviceHistory.setModifiedDate(new Date());
				deviceHistory.add(updateDeviceHistory);
			}
			deviceRepository.save(device);
			deviceHistoryRepository.save(deviceHistory);
			response.setError(false);
			response.setMessage(DeviceConstants.DEVICE_UPDATED_SUCCESSFULLY);
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setError(true);
			response.setMessage(DeviceConstants.INVALID_DEVICE_ID);
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return response;
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#listDevice(java.lang.String, int,
	 * int) API for listing the AMR devices. Search data along with pagination
	 * details are received from the request param(URL). If the search data is
	 * null/blank/empty, all the devices whose state is "Active" in the DB are sent
	 * in the response. Else the search parameters are filtered and the result is
	 * sent in the response. The response is sent with data such as status, error,
	 * message(in this case "list of devices") and the actual list of devices are
	 * sent to the front end.
	 */

	@Override
	public ListResponse<List<DeviceResponseDto>> listDevice(String search, int page, int size) {
		ListResponse<List<DeviceResponseDto>> response = new ListResponse<>();
		List<Device> device = null;
		if (search == null || search.isEmpty())
			device = deviceRepository.findByIsActive(true, createPageRequest(page, size));
		else
			device = deviceRepository
					.findByDeviceIdContainsIgnoreCaseOrSerialNumberContainsIgnoreCaseOrDeviceEuiContainsIgnoreCaseAndIsActive(
							search, search, search, true, createPageRequest(page, size));

		if (device.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.setError(true);
			response.setMessage(DeviceConstants.DEVICE_NOT_FOUND);
		} else {
			List<DeviceResponseDto> deviceResponseList = new ArrayList<>();
			device.forEach(l -> deviceResponseList.add(convertToDeviceResponseDto(l)));
			response.setData(deviceResponseList);
			response.setCount(device.size());
			response.setTotalRecords(getTotalRecords(search));
			response.setStatus(HttpServletResponse.SC_OK);
			response.setError(false);
			response.setMessage(DeviceConstants.DEVICE_LIST_RESPONSE);
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#getActiveDevices(int, int) API for
	 * listing all the active AMR devices. Pagination details are received from the
	 * request param(URL). All the devices whose state is not "InActive" is sent
	 * fetched from the DB with pagination to curtail the no. of entities that are
	 * returned. The response is sent with data such as status, error, message(in
	 * this case "list of devices") and the actual list of devices are sent to the
	 * front end.
	 * 
	 */
	@Override
	public ListResponse<List<DeviceResponseDto>> getActiveDevices(int page, int size) {
		ListResponse<List<DeviceResponseDto>> response = new ListResponse<>();
		List<Device> device = null;
		DeviceStatus deviceStatus = deviceStatusRepository.findByStatusAndIsActive("InActive", true);
		device = deviceRepository.findByStatusNot(deviceStatus.getStatusId(), createPageRequest(page, size));
		if (device.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.setError(true);
			response.setMessage(DeviceConstants.DEVICE_NOT_FOUND);
		} else {
			List<DeviceResponseDto> deviceResponseList = new ArrayList<>();
			device.forEach(l -> deviceResponseList.add(convertToDeviceResponseDto(l)));
			response.setData(deviceResponseList);
			response.setCount(device.size());
			response.setTotalRecords(deviceRepository.findByStatusNot("InActive").size());
			response.setStatus(HttpServletResponse.SC_OK);
			response.setError(false);
			response.setMessage(DeviceConstants.DEVICE_LIST_RESPONSE);
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#deviceHistory(int, int, int) This
	 * method is used to return the previous statuses of the device and the userId
	 * of the user who changed it along with the modified date.
	 */
	@Override
	public ListResponse<List<DeviceHistoryResponseDto>> deviceHistory(String deviceId, int page, int size) {
		ListResponse<List<DeviceHistoryResponseDto>> response = new ListResponse<>();
		List<DeviceHistory> deviceHistory = deviceHistoryRepository.findByDeviceId(deviceId,
				createSortPageRequest(page, size));
		if (deviceHistory.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.setError(true);
			response.setMessage(DeviceConstants.DEVICE_NOT_FOUND);
		} else {
			List<DeviceHistoryResponseDto> deviceResponseList = new ArrayList<>();
			deviceHistory.forEach(l -> deviceResponseList.add(convertToDeviceHistoryResponseDto(l)));
			response.setData(deviceResponseList);
			response.setCount(deviceResponseList.size());
			response.setTotalRecords(deviceHistoryRepository.findByDeviceId(deviceId).size());
			response.setStatus(HttpServletResponse.SC_OK);
			response.setError(false);
			response.setMessage(DeviceConstants.DEVICE_HISTORY_RESPONSE);
		}
		return response;
	}

	/*
	 * converts the data from device history to device history response
	 */
	private DeviceHistoryResponseDto convertToDeviceHistoryResponseDto(DeviceHistory deviceHistory) {
		return modelMapper.map(deviceHistory, DeviceHistoryResponseDto.class);

	}

	private Pageable createSortPageRequest(int page, int size) {
		return new PageRequest(page, size, new Sort(Sort.Direction.ASC, "modified_date"));
	}

	/*
	 * This method gets the value of the total number of records present in the DB.
	 * If there is any search constrains, it returns the total number of the the
	 * search results
	 */
	private Integer getTotalRecords(String search) {
		if (search != null)
			return deviceRepository
					.findByDeviceIdContainsIgnoreCaseOrSerialNumberContainsIgnoreCaseAndIsActive(search, search, true)
					.size();
		else

			return deviceRepository.findByIsActive(true).size();

	}

	private Pageable createPageRequest(int page, int size) {
		return new PageRequest(page, size, new Sort(Sort.Direction.DESC, "_id"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#deleteDevice(com.tcl.devices.dto.
	 * DeviceDeleteRequestDto) API for deleting an AMR device. The device is
	 * received from the request and is used to see if there is any device present
	 * in the DB with the same deviceID. If the device (with the specified ID) is
	 * present, the "isActive" status of the device is changed to false. Else an
	 * error(mentioning that there's no device present with the specified ID) is
	 * sent in the response. The response is then filled with details like status,
	 * message and error codes and is sent.
	 */
	@Override
	public Response<Boolean> deleteDevice(DeviceDeleteRequestDto request) {
		Response<Boolean> response = new Response<>();
		List<Device> device = deviceRepository.findByDeviceIdInAndIsActive(request.getDeviceId(), true);
		if (!device.isEmpty()) {
			for (Device deleteDevice : device) {
				deleteDevice.setIsActive(false);
				deleteDevice.setModifiedDate(new Date());
			}
			deviceRepository.save(device);
			response.setMessage(DeviceConstants.DEVICE_DELETED_SUCCESSFULLY);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setError(false);
		} else {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.setError(true);
			response.setMessage(DeviceConstants.DEVICE_NOT_FOUND);
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#getOneDevice(java.lang.String)
	 * This method returns the details of one specific device. The device ID is got
	 * from the request and then is used to find that particular device in the DB.
	 */
	@Override
	public Response<DeviceResponseDto> getOneDevice(String deviceId) {
		Response<DeviceResponseDto> response = new Response<>();
		Optional<Device> optional = deviceRepository.findByDeviceIdAndIsActive(deviceId, true);
		if (optional.isPresent()) {
			response.setData(convertToDeviceResponseDto(optional.get()));
			response.setError(false);
			response.setMessage(DeviceConstants.DEVICE_DETAILS);
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			response.setError(true);
			response.setMessage(DeviceConstants.DEVICE_NOT_FOUND);
		}

		return response;
	}

	/*
	 * This method splits the functionalities of the get device method. It maps the
	 * objects of two different classes and copies the data from one to the other It
	 * helps us to avoid traditional mapping of objects.
	 */
	private DeviceResponseDto convertToDeviceResponseDto(Device device) {
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		return modelMapper.map(device, DeviceResponseDto.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#getDeviceStatus() This method is
	 * use to get the status of the device. It acts as a master data for device
	 * status.
	 */
	@Override
	public Response<List<DeviceStatusResponseDto>> getDeviceStatus() {
		Response<List<DeviceStatusResponseDto>> response = new Response<>();
		List<DeviceStatus> deviceStatus = deviceStatusRepository.findByIsActive(true);
		List<DeviceStatusResponseDto> deviceStatusResponseList = new ArrayList<>();
		deviceStatus.forEach(l -> deviceStatusResponseList.add(convertToDeviceStatusResponseDto(l)));
		response.setError(false);
		response.setData(deviceStatusResponseList);
		response.setMessage(DeviceConstants.DEVICE_STATUS_RESPONSE);
		response.setStatus(HttpServletResponse.SC_OK);
		return response;
	}

	private DeviceStatusResponseDto convertToDeviceStatusResponseDto(DeviceStatus deviceStatus) {
		return modelMapper.map(deviceStatus, DeviceStatusResponseDto.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#addStatus(com.tcl.devices.model.
	 * DeviceStatus) This method is use to add new status to the previously
	 * available master data for device status.
	 */
	@Override
	public Response<Boolean> addStatus(DeviceStatus request) {
		deviceStatusRepository.save(request);
		Response<Boolean> response = new Response<>();
		response.setError(false);
		response.setMessage(DeviceConstants.DEVICE_ADD_STATUS_RESPONSE);
		response.setStatus(HttpServletResponse.SC_OK);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tcl.devices.service.DeviceService#addNewAssign(com.tcl.devices.dto.
	 * AssignDeviceRequestDto) This method is used to assign a device to the user
	 * that can be fetched from DB.
	 */
	@Override
	public Response<Boolean> addNewAssign(AssignDeviceRequestDto request) {
		Response<Boolean> response = new Response<>();
		List<AssignDevice> assignDevice = new ArrayList<>();
		AssignDevice assign = null;
		List<Device> updateDevice = deviceRepository.findByDeviceIdInAndIsActive(request.getDeviceId(), true);
		if (updateDevice.isEmpty())
			throw new DeviceAlreadyPresent(DeviceConstants.INVALID_DEVICE_ID);
		for (Device device : updateDevice) {
			device.setAssigned(request.getAssignedTo());
			device.setModifiedDate(new Date());
			device.setModifiedBy(request.getUserId());
			assign = new AssignDevice();
			assign.setAssignedTo(request.getAssignedTo());
			assign.setDeviceId(device.getDeviceId());
			assign.setCreatedDate(new Date());
			assignDevice.add(assign);
		}
		deviceRepository.save(updateDevice);
		deviceAssignmentRepository.save(assignDevice);

		response.setError(false);
		response.setMessage(DeviceConstants.DEVICE_ASSIGN_RESPONSE);
		response.setStatus(HttpServletResponse.SC_OK);
		return response;
	}

	@Override
	public ListResponse<List<DeviceResponseDto>> filterListCustomers(List<String> filters, String sortByFieldName,
			String sortType, Integer page, Integer size) {
		ListResponse<List<DeviceResponseDto>> response = new ListResponse<>();

		// need to implement
		return response;
	}

	/*
	 * This method is used to add a device history object every time it gets called
	 * in the methods above. This method is used to insert a data into the DB every
	 * time the status of the device is changed. The status can be changed in the
	 * status change API and device update API.
	 */
	public void addDeviceHistory(String userId, String deviceId, String status) {
		DeviceHistory deviceHistory = new DeviceHistory();
		deviceHistory.setDeviceId(deviceId);
		deviceHistory.setStatus(status);
		deviceHistory.setModifiedBy(userId);
		deviceHistory.setModifiedDate(new Date());
		deviceHistoryRepository.save(deviceHistory);
	}

}