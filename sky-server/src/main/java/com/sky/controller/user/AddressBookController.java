package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端-地址簿接口")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的地址列表
     * @return
     */

    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址")
    public Result<List<AddressBook>> list()
    {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list=addressBookService.list(addressBook);
        return Result.success(list);
    }
    /**
     * 添加地址
     */
    @PostMapping
    @ApiOperation("添加地址")
    public Result save(@RequestBody AddressBook addressBook)
    {
        addressBookService.save(addressBook);
        return Result.success();
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id)
    {
        AddressBook addressBook=addressBookService.getById(id);
        return Result.success(addressBook);
    }
    /**
     * 修改地址
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result update(AddressBook addressBook)
    {
        addressBookService.update(addressBook);
        return Result.success();
    }
    /**
     * 设置默认地址
     */
    @PutMapping("default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook)
    {
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除地址")
    public Result delete(@RequestParam Long id)
    {
        addressBookService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault() {
        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);

        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }

        return Result.error("没有查询到默认地址");
    }


}
