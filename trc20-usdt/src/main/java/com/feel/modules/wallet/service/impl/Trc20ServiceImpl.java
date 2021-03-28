package com.feel.modules.wallet.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feel.common.utils.Constant;
import com.feel.modules.wallet.entity.Coin;
import com.feel.modules.wallet.service.Trc20Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tron.utils.HttpClientUtils;
import org.tron.utils.TronUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.protos.Protocol;
import org.tron.utils.HttpClientUtils;
import org.tron.utils.TronUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zz
 * @Description:
 * @Date: 3:30 PM 3/15/21
 * @Modified By
 */
@Service
@Slf4j
public class Trc20ServiceImpl implements Trc20Service {



    @Autowired
    private Coin coin;


    /**
     * 创建地址
     * @param accountName
     * @return
     */
    @Override
    public String createNewAddress(String accountName)  {
        Map<String, String> map = TronUtils.createAddress();

        return map.get("address");
    }

    /**
     * 区块高度
     * @return
     */
    @Override
    public Integer height() {
        Integer count = 0;
        Integer height = count - 1;
        log.info("block height [{}]" , height);
        return height;
    }

    /**
     * 获取trc地址总资产
     * @param address
     * @return
     */
    @Override
    public BigDecimal getTrcBalance(String address) throws IOException {
        String url = Constant.tronUrl + "/wallet/triggerconstantcontract";
        JSONObject param = new JSONObject();
        param.put("owner_address",TronUtils.toHexAddress(address));
        param.put("contract_address",TronUtils.toHexAddress(Constant.contract));
        param.put("function_selector","balanceOf(address)");
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Address(TronUtils.toHexAddress(address).substring(2)));
        param.put("parameter",FunctionEncoder.encodeConstructor(inputParameters));
        String result = HttpClientUtils.postJson(url, param.toJSONString());
        BigDecimal amount = BigDecimal.ZERO;
        if(StringUtils.isNotEmpty(result)){
            JSONObject obj = JSONObject.parseObject(result);
            JSONArray results = obj.getJSONArray("constant_result");
            if(results != null && results.size() > 0){
                BigInteger _amount = new BigInteger(results.getString(0),16);
                amount = new BigDecimal(_amount).divide(Constant.decimal,6, RoundingMode.FLOOR);
            }
        }
        log.info(String.format("账号%s的balance=%s",address,amount.toString()));
        return new BigDecimal(amount.toString());
    }

    /**
     * 交易trc
     * @param from
     * @param toAddress
     * @param amount
     * @param fee
     * @return
     */
    @Override
    public String transferTrc(String from, String toAddress, BigDecimal amount, BigDecimal fee) throws Throwable {

        String privateKey = "7a2195d52c42c34a8de11633de7fdfbbf6883d2e95918ccd845230629fd95768";
       // TronUtils.getAddressByPrivateKey(privateKey);

        String ownerAddress = TronUtils.getAddressByPrivateKey(privateKey);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("contract_address", TronUtils.toHexAddress(Constant.contract));
        jsonObject.put("function_selector", "transfer(address,uint256)");
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Address(TronUtils.toHexAddress(toAddress).substring(2)));
        inputParameters.add(new Uint256(amount.multiply(Constant.decimal).toBigInteger()));
        String parameter = FunctionEncoder.encodeConstructor(inputParameters);
        jsonObject.put("parameter", parameter);
        jsonObject.put("owner_address", TronUtils.toHexAddress(ownerAddress));
        jsonObject.put("call_value", 0);
        jsonObject.put("fee_limit", 6000000L);
        String trans1 = HttpClientUtils.postJson(Constant.tronUrl + "/wallet/triggersmartcontract", jsonObject.toString());
        JSONObject result = JSONObject.parseObject(trans1);
        if (result.containsKey("Error")) {
            return null;
        }
        JSONObject tx = result.getJSONObject("transaction");
        tx.getJSONObject("raw_data").put("data", Hex.toHexString("我是Tricky".getBytes()));//填写备注
        String txid = TronUtils.signAndBroadcast(Constant.tronUrl, privateKey, tx);
        if (txid != null) {
            log.info("交易Id:" + txid);
            return  txid;
        }else {
            return  null;
        }
    }

    /**
    * @Description: trx
    * @Param:
    * @return:
    * @Author: lhp
    * @Date: 2021-03-27 23:03
    **/
    public BigDecimal getBalance(String address) throws IOException{
        String url = Constant.tronUrl + "/wallet/getaccount";
        JSONObject param = new JSONObject();
        param.put("address", TronUtils.toHexAddress(address));
        String result = HttpClientUtils.postJson(url, param.toJSONString());
        BigInteger balance = BigInteger.ZERO;
        if (!StringUtils.isEmpty(result)) {
            JSONObject obj = JSONObject.parseObject(result);
            BigInteger b = obj.getBigInteger("balance");
            if(b != null){
                balance = b;
            }
        }
        return new BigDecimal(balance).divide(Constant.decimal,6, RoundingMode.FLOOR);


    }

    /**
     * 交易trx
     * @param from
     * @param toAddress
     * @param amount
     * @param fee
     * @return
     */
    public String transfer(String from, String toAddress, BigDecimal amount, BigDecimal fee) throws Throwable{
        String privateKey = "7a2195d52c42c34a8de11633de7fdfbbf6883d2e95918ccd845230629fd95768";

        String url = Constant.tronUrl + "/wallet/createtransaction";
        JSONObject param = new JSONObject();
        param.put("owner_address",TronUtils.toHexAddress(TronUtils.getAddressByPrivateKey(privateKey)));
        param.put("to_address",TronUtils.toHexAddress(toAddress));
        param.put("amount",amount.multiply(Constant.decimal).toBigInteger());
        String _result = HttpClientUtils.postJson(url, param.toJSONString());
        String txid = null;//交易id
        if(StringUtils.isNotEmpty(_result)){
            JSONObject transaction = JSONObject.parseObject(_result);
            transaction.getJSONObject("raw_data").put("data", Hex.toHexString("这里是备注信息".getBytes()));
            txid = TronUtils.signAndBroadcast(Constant.tronUrl, privateKey, transaction);
            log.info(txid);
            return txid;
        }else {
            return  null;
        }
    }


}