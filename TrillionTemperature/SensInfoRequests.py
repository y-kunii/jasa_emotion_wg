# coding:utf-8
#[準備]
#以下を実施し、requestsをインストールしてください。
# > pip install requests

import requests

AZURU_URL	= "https://<IoT-HubのURL>.amazonaws.com/logger/api/trillion_log"
TRILLION_TEMP_MSG = "/trillionTemp/"
TRILLION_HUMID_MSG = "/trillionHumid/"
FAN_ACT_TIME_MSG = "/fanActTime/"
FAN_ACT_TEMP_MSG = "/fanActTemp/"
FAN_ACT_HUMID_MSG = "/fanActHumid/"

class SensInfoRequests:
	
	def __init__(self):
		return
	
	def __PostAccess(self, msg):
		try:
			print "PostStart"
			url = AZURU_URL + msg
			print url
			res = requests.post(url)
			print (res.text)
			print (res.status_code)
		except:
			print "Error"
		return
		
	def PostTrillionTemp(self, temp):
		msg = TRILLION_TEMP_MSG + str(temp)
		self.__PostAccess(msg)
		return

	def PostTrillionHumid(self, humid):
		msg = TRILLION_HUMID_MSG + str(humid)
                self.__PostAccess(msg)
		return

	def PostFanActTime(self, actTime):
		msg = FAN_ACT_TIME_MSG + str(actTime)
                self.__PostAccess(msg)
		return

	def PostFanActTemp(self, actTemp):
		msg = FAN_ACT_TEMP_MSG + str(actTemp)
                self.__PostAccess(msg)
		return

	def PostFanActHumid(self, actHumid):
		msg = FAN_ACT_HUMID_MSG + str(actHumid)
                self.__PostAccess(msg)
		return
		
def main():
	print "開始"
	sensInfoRequestsObj = SensInfoRequests()
	sensInfoRequestsObj.PostTrillionTemp(15)
	sensInfoRequestsObj.PostTrillionHumid(20)
	sensInfoRequestsObj.PostFanActTime(30)
	sensInfoRequestsObj.PostFanActTemp(40)
	sensInfoRequestsObj.PostFanActHumid(60)
	print "終了"
	
if __name__ == "__main__":
	main()

