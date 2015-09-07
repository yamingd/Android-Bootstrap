package com.argo.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 7/17/15.
 */
public class DataIndexBuilder<T extends DataIndexBuilder.DataIndex> {

    /**
     *
     */
    public static interface DataIndex {

        String getDataIndexKeyValue();

    }

    public static interface DataIndexKeyGetter<T>{
        /**
         *
         * @param o
         * @return
         */
        String get(T o);
    }

    public class DataItemValueComparator implements Comparator<T>{

        @Override
        public int compare(DataIndex lhs, DataIndex rhs) {
            String lchsCode = lhs.getDataIndexKeyValue();
            String rchsCode = rhs.getDataIndexKeyValue();

            return DataIndexBuilder.this.compare(lchsCode, rchsCode);
        }
    }

    public int compare(String lchsCode, String rchsCode) {

        char lFirstLetter = lchsCode.charAt(0);
        char rFirstLetter = rchsCode.charAt(0);

        if (lFirstLetter >= 65 && lFirstLetter <= 90){
            if (rFirstLetter < 65 || rFirstLetter > 90){
                return 1;
            }
        }else{
            if (rFirstLetter >= 65 && rFirstLetter <= 90){
                return -1;
            }
        }


        return lchsCode.compareTo(rchsCode);
    }

    public class DataIndexMarkEntry implements Map.Entry<String, Integer>{

        private String key = "";
        private int value = -1;

        public DataIndexMarkEntry(String key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(Integer object) {
            this.value = object;
            return this.value;
        }

        public boolean isHeader(){
            return this.value == -1;
        }

        public boolean isRecord(){
            return this.value >= 0;
        }
    }


    private List<T> dataList = null;
    private List<String> keyList = null;
    private List<DataIndexMarkEntry> indexMark = null;
    private Comparator<T> comparator = new DataItemValueComparator();

    public DataIndexBuilder(){
        keyList = new ArrayList<String>();
        indexMark = new ArrayList<DataIndexMarkEntry>();
    }

    /**
     * 分组
     * @return
     */
    public DataIndexBuilder group(final DataIndexKeyGetter<T> getter){
        keyList.clear();
        indexMark.clear();

        if (this.dataList == null){
            return this;
        }
        Collections.sort(this.dataList, new Comparator<T>() {
            @Override
            public int compare(T litem, T ritem) {

                String lchsCode = getter.get(litem);
                String rchsCode = getter.get(ritem);

                return DataIndexBuilder.this.compare(lchsCode, rchsCode);
            }
        });

        //Timber.d("%s, ######### this.dataList=%s", this, this.dataList);

        int len = dataList.size();
        int prevc = 0;
        for (int i = 0; i < len; i++) {
            String keyValue = getter.get(dataList.get(i));
            prevc = prepareIndex(prevc, i, keyValue);
        }

        //Timber.d("indexMark size: %d", indexMark.size());

        Collections.sort(keyList);

        return this;
    }

    public int prepareIndex(int prevc, int i, String keyValue) {
        char c = keyValue.charAt(0);
        if (c < 65 || c > 90) {
            c = 35; // "#"
        }
        String k = String.valueOf(c);
        if (!keyList.contains(k)){
            keyList.add(k);
        }

        if (c != prevc){
            DataIndexMarkEntry entry = new DataIndexMarkEntry(k, -1);
            indexMark.add(entry);
            prevc = c;
        }

        DataIndexMarkEntry entry = new DataIndexMarkEntry(k, i);
        indexMark.add(entry);
        return prevc;
    }

    /**
     * 分组
     * @return
     */
    public DataIndexBuilder group(){
        keyList.clear();
        indexMark.clear();

        if (this.dataList == null){
            return this;
        }
        Collections.sort(this.dataList, this.comparator);
        //Timber.d("%s, ######### this.dataList=%s", this, this.dataList);

        int len = dataList.size();
        int prevc = 0;
        for (int i = 0; i < len; i++) {
            String keyValue = dataList.get(i).getDataIndexKeyValue();
            prevc = prepareIndex(prevc, i, keyValue);
        }

        //Timber.d("indexMark size: %d", indexMark.size());

        Collections.sort(keyList);

        return this;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public int size(){
        return indexMark.size();
    }

    /**
     * 取得分组的字母列表
     * @return
     */
    public List<String> getKeyList() {
        return keyList;
    }

    /**
     * 是分组头部
     * @return
     */
    public boolean isSectionHeader(int position){
        return indexMark.get(position).isHeader();
    }

    /**
     * 分组字母
     * @param position
     * @return
     */
    public String getSectionHeader(int position){
        return indexMark.get(position).getKey();
    }

    /**
     *
     * @param letter
     * @return
     */
    public int getSectionIndex(String letter){
        char c = letter.charAt(0);
        if (c < 65 || c > 90) {
            c = 35; // "#"
        }
        String v = String.valueOf(c);
        for (int i = 0; i < indexMark.size(); i++) {
            if (indexMark.get(i).getKey().equals(v)){
                return i;
            }
        }
        return 0;
    }

    /**
     * 是不是记录
     * @return
     */
    public boolean isSectionItem(int position){
        return indexMark.get(position).isRecord();
    }

    /**
     * 获取数据记录
     * @param position
     * @return
     */
    public T get(int position){
        int index = indexMark.get(position).getValue();
        if (index == -1){
            return null;
        }
        T value = dataList.get(index);
        return value;
    }

    /**
     * 重置数据
     * @param list
     */
    public DataIndexBuilder setRecords(List<T> list){
        dataList = list;
        return this;
    }

    /**
     * 追加数据
     * @param list
     */
    public DataIndexBuilder appendRecords(List<T> list){
        if (dataList == null){
            dataList = new ArrayList<T>();
        }
        dataList.addAll(list);
        return this;
    }

    /**
     * 清除数据
     * @return
     */
    public DataIndexBuilder clear(){
        if (dataList == null){
            return this;
        }
        dataList.clear();
        return this;
    }
}
