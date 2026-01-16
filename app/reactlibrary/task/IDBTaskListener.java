package com.example.myapplication.reactlibrary.task;

public interface IDBTaskListener {
	
	void onPreExecute();
	void onPostExecute();
	void onDoInBackground();
}
