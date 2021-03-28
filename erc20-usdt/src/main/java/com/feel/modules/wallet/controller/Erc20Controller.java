package com.feel.modules.wallet.controller;

import cn.hutool.core.util.ObjectUtil;
import com.feel.modules.wallet.entity.Account;
import com.feel.modules.wallet.entity.Contract;
import com.feel.modules.wallet.service.AccountService;
import com.feel.modules.wallet.service.Erc20Service;
import com.feel.modules.wallet.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @Author: zz
 * @Description:
 * @Date: 3:24 PM 3/15/21
 * @Modified By
 */
@RestController
@RequestMapping("/rpc")
@Api(tags = "币种信息 : erc20-usdt" ,value = "erc20-usdt")
@Slf4j
public class Erc20Controller extends WalletController<Erc20Service>{

    @Autowired
    private AccountService accountService;



    @Override
    @GetMapping("/getNewAddress")
    @ApiOperation("获取新地址")
    public R<Account> getNewAddress(String accountName) {
        String newAddress = null;
        Account account = accountService.findByName(accountName);
        if(ObjectUtil.isNotEmpty(account)) {
            return R.ok(account);
        }

        try {
            newAddress = walletService.createNewAddress(accountName);
        } catch (Exception e) {
            log.error("获取地址失败",e);
            return R.error("获取地址失败");
        }
        account = accountService.saveOne(accountName, newAddress);
        return R.ok(account);
    }

    @Override
    @GetMapping("/height")
    @ApiOperation("获取区块高度")
    public R<Long> getBlockHeight() {
        Long height = null;
        try {
            height = walletService.height();
        } catch (Exception e) {
            return R.error("获取高度失败");
        }
        return R.ok(height);
    }


    @Override
    @GetMapping("/balances")
    @ApiOperation("获取节点总资产")
    public R balances() {
        //TODO
        return null;
    }

    @Override
    @GetMapping("/balance")
    @ApiOperation("获取地址总资产")
    public R<BigDecimal> balance(String address) {
        BigDecimal balance = null;
        try {
            balance = walletService.getBalance(address);
        } catch (Exception e) {
            return R.error("获取地址总资产失败");
        }
        return R.ok(balance);
    }

    @Override
    @GetMapping("/transfer")
    @ApiOperation("交易")
    public R<String> transfer(String from, String to, BigDecimal amount, BigDecimal fee) {
        String txid = null;
        try {
            txid = walletService.transfer(from,to,amount,fee);
        } catch (RuntimeException e) {
            log.error("转账失败",e);
            return R.error("转账失败");
        } catch (Exception e) {
            log.error("转账失败" ,e);
            return R.error("转账失败");
        }
        return R.ok(txid);
    }


}