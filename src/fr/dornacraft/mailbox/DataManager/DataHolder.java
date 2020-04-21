package fr.dornacraft.mailbox.DataManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
/**
 * Contient les Data des joueurs
 * @author Bletrazer
 *
 */
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
		Data res = null;

		for (Integer index = 0; index < this.getDataList().size() && res == null; index++) {
			Data data = this.getDataList().get(index);
			if (data.getId().equals(id)) {
				res = data;
			}
		}
		
		return res;
	}

	public void addData(Data data) {
		this.getDataList().add(data);
	}

	public void removeData(Long id) {
		Iterator<Data> it = this.getDataList().iterator();
		Boolean stop = false;
		
		while(it.hasNext() && !stop) {
			Data data = it.next();
			
			if(data.getId().equals(id)) {
				stop = true;
				it.remove();
			}
			
		}
	}

	public UUID getOwnerUuid() {
		return this.ownerUuid;
	}

	private void setOwnerUuid(UUID ownerUuid) {
		this.ownerUuid = ownerUuid;
	}

}
