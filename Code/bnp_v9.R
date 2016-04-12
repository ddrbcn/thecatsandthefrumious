
if (!require("xgboost")) install.packages("xgboost")
if (!require("mice")) install.packages("mice")
if (!require("VIM")) install.packages("VIM")
if (!require("Matrix")) install.packages("Matrix")
if (!require("yaImpute")) install.packages("yaImpute")
library(xgboost)
library(mice)
library(VIM)
library(Matrix)
library(yaImpute)
library("randomForest")

set.seed(2016)

train_o <- read.csv("~/Documents/Kaggle/BNP/data/train.csv")
test_o <- read.csv("~/Documents/Kaggle/BNP/data/test.csv")

##### Removing columns

# train<-subset(train_o,select=-c(v22)) #,v30,v47,v52,v56,v79))
# test<-subset(test_o,select=-c(v22)) #,v30,v47,v52,v56,v79))
train <- train_o
test  <- test_o
for (var in 1:ncol(train)) {
  if (class(train[,var]) %in% c("character", "factor")) {
        
    
      dat<-train[,var]
      train[paste("freq", var, sep = "_")] <- lapply(dat, function(x) {sum((train$target==1)&(dat==x))})
   
    
  }
}

#transforming v22
train$v22 <- as.numeric(as.factor(as.character(train$v22)))
test$v22  <- as.numeric(as.factor(as.character(test$v22)))

#### Removing IDs
train$ID <- NULL
test.id <- test$ID
test$ID <- NULL

##### Extracting TARGET
train.y <- train$target
train$target <- NULL

##### copy
train_copy <- train
test_copy  <-  test

#train_copy[train_copy==""]<-"MISSING"
#test_copy[test_copy==""]<-"MISSING"

Mode <- function (x, na.rm) {
  xtab <- table(x)
  xmode <- names(which(xtab == max(xtab)))
  return(xmode)
}

imputer<-function(df_test){
  
  for (var in 1:ncol(df_test)) {
    if (class(df_test[,var])=="numeric") {
      #df_test[is.na(df_test[,var]),var] <- mean(df_test[,var], na.rm = TRUE)
      df_test[is.na(df_test[,var]),var] <- 1000000000
    } else if (class(df_test[,var]) %in% c("character", "factor")) {
      df_test[is.na(df_test[,var]),var] <- Mode(df_test[,var], na.rm = TRUE)
      for(level in unique(df_test[,var])){
        df_test[paste("dummy", var,level, sep = "_")] <- ifelse(df_test[,var] == level, 1, 0)
      }
      
    }
  }
  return(df_test)
}
imputer0<-function(df_test){
  
  for (var in 1:ncol(df_test)) {
    if (class(df_test[,var])=="numeric") {
      #df_test[is.na(df_test[,var]),var] <- mean(df_test[,var], na.rm = TRUE)
      df_test[is.na(df_test[,var]),var] <- 0
    }
  }
  return(df_test)
}

train_no_missing<-imputer(train_copy)
test_no_missing<-imputer(test_copy)

test_no_missing[,setdiff(colnames(train_no_missing), colnames(test_no_missing))]<-0
test_no_missing<-imputer0(test_no_missing)
test_no_missing<-test_no_missing[,colnames(train_no_missing)]


# for (name in colnames(train_copy[,sapply(train, is.factor)])){
#   train_no_missing[,name] <- as.numeric(as.factor(as.character(train_no_missing[,name])))
#   test_no_missing[,name] <- as.numeric(as.factor(as.character(test_no_missing[,name])))
#   
# } 

test.new<-test_no_missing[, !sapply(train_no_missing, is.factor)]
train.new<-train_no_missing[, !sapply(train_no_missing, is.factor)]

combi<-train_no_missing[, !sapply(train_no_missing, is.factor)]
combi$target <-  train.y
fit <- randomForest(as.factor(target) ~ ., data = combi, importance=TRUE, ntree=1)
fimportance<-data.frame(importance(fit))
fimportance<-fimportance[order(-fimportance$MeanDecreaseAccuracy),]
important<-row.names(fimportance)[1:246]

test.new<-test.new[,important]
train.new<-train.new[,important]
#test.new<-test_no_missing
#train.new<-train_no_missing



train.new$target <- train.y

train.new <- sparse.model.matrix(target ~ ., data = train.new)

dtrain <- xgb.DMatrix(data=train.new, label=train.y)
watchlist <- list(train=dtrain)

set.seed(2016)
param <- list(  objective           = "binary:logistic", 
                booster             = "gbtree",
                eval_metric         = "logloss",
                eta                 = 0.04,
                max_depth           = 9,
                subsample           = 0.95,
                colsample_bytree    = 0.85,
                #scale_pos_weight    = 73012/3008,
                min_child_weight    = 5,
                nthread             = 2
)

clf <- xgb.train(   params              = param, 
                    data                = dtrain, 
                    nrounds             = 295, 
                    verbose             = 1,
                    watchlist           = watchlist,
                    maximize            = FALSE
)


test.new$target <- -1
test.new <- sparse.model.matrix(target ~ ., data = test.new)

preds <- predict(clf, test.new)
submission <- data.frame(ID=test.id, PredictedProb=preds)
cat("saving the submission file\n")
write.csv(submission, "submissionV8_295d9eta04sc9585.csv", row.names = F)
combi<-train_no_missing[, !sapply(train_no_missing, is.factor)]
combi$target <-  train.y
fit <- randomForest(as.factor(target) ~ ., data = combi, importance=TRUE, ntree=1)
