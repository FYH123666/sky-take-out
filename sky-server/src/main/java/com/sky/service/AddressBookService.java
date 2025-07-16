package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /**
     * 查询当前登录用户的地址列表
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 添加地址
     * @param addressBook
     */
    void save(AddressBook addressBook);

    /**
     * 根据id查询地址
     */
    AddressBook getById(Long id);


    /**
     * 修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 设置默认地址
     */
    void setDefault(AddressBook addressBook);

    /**
     * 删除地址
     * @param id
     */
    void deleteById(Long id);


}
