package main;

import java.util.ArrayList;

public class TxHandler {

	
	private UTXOPool utxpool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	this.utxpool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	
    	// IMPLEMENT THIS
    	double prevSum = 0;
    	double currSum = 0;
    	for(int i=0; i< tx.numInputs(); i++){
    		UTXOPool uniquePool = new UTXOPool();
    		Transaction.Input input = tx.getInput(i);
    		UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
    		Transaction.Output output = this.utxpool.getTxOutput(utxo);
    		if(!utxpool.contains(utxo)) return false;
    		if(!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) return false;
    		if(uniquePool.contains(utxo)) return false;
    		uniquePool.addUTXO(utxo, output);
    		prevSum += output.value;
    	}
    	
    	for(Transaction.Output output: tx.getOutputs()){
    		if(output.value < 0) return false;
    		currSum += output.value;
    	}
    	
    	if(prevSum < currSum) return false;
    	
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	
    	// IMPLEMENT THIS
    	ArrayList<Transaction> validTxs = new ArrayList<>();
    	
    	for(Transaction tx : possibleTxs){
    		if(isValidTx(tx)){
    			validTxs.add(tx);
    			
    			for(Transaction.Input input : tx.getInputs()){
    				UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
    				this.utxpool.removeUTXO(utxo);
    			}
    			
    			for(int i=0;i<tx.numOutputs();i++){
    				Transaction.Output output = tx.getOutput(i);
    				UTXO utxo = new UTXO(tx.getHash(), i);
    				this.utxpool.addUTXO(utxo, output);
    			}
    		}
    	}
		return validTxs.toArray(new Transaction[validTxs.size()]);
        
    }

}
