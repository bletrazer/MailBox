package fr.bletrazer.mailbox.DataManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class DataHolder {
	private UUID ownerUuid;
	private List<Data> listData = new ArrayList<>();
	
	public DataHolder(UUID owner, List<Data> dataList) {
		this.setOwnerUuid(owner);
		this.setDataList(dataList);
	}

	public List<Data> getDataList() {
		return listData;
	}

	private void setDataList(List<Data> dataList) {
		listData = dataList;
	}

	public Data getData(Long id) {
		return this.listData.stream().filter(e -> e.getId() == id).findAny().orElse(null);
	}

	public void addData(Data data) {
		this.getDataList().add(data);
	}

	public void removeData(Long id) {
		Iterator<Data> it = this.getDataList().iterator();
		
		while(it.hasNext()) {
			Data data = it.next();
			
			if(data.getId().equals(id)) {
				it.remove();
				break;
			}
			
		}
	}
	
	public void removeDatas(List<Long> idList ) {
		for(Long id : idList) {
			this.removeData(id);
		}
		
	}

	public UUID getOwnerUuid() {
		return this.ownerUuid;
	}

	private void setOwnerUuid(UUID ownerUuid) {
		this.ownerUuid = ownerUuid;
	}

}
