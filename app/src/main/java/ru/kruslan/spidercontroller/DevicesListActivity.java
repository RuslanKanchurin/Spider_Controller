package ru.kruslan.spidercontroller;




import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class DevicesListActivity extends ListActivity {

    BluetoothAdapter bluetoothAdapter;
    ArrayList< String> pairedDeviceArrayList;
    ArrayAdapter< String> pairedDeviceAdapter;
    ListView listViewPairedDevice;
    static BluetoothDevice device2;
    private UUID myUUID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        listViewPairedDevice=getListView();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device.getName() + "\n" + device.getAddress());
            }
            pairedDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedDeviceArrayList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        listViewPairedDevice.setVisibility(View.GONE);
        String  itemValue = (String) listViewPairedDevice.getItemAtPosition(position);
        String MAC = itemValue.substring(itemValue.length() - 17);
        //device2 = bluetoothAdapter.getRemoteDevice(MAC);
        /*MainActivity.ThreadConnectBTdevice myThreadConnectBTdevice = new MainActivity.ThreadConnectBTdevice(device2);
        myThreadConnectBTdevice.start();
*/
        Intent intent=new Intent();
        intent.putExtra("MAC",MAC);
        setResult(RESULT_OK,intent);
        finish();

    }



}