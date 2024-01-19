package org.demo.oss.controller;
import org.demo.oss.service.impl.StorageServiceImpl;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

/**
* 存储桶表(storage)表控制层
*/
@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageServiceImpl storageServiceImpl;

}
